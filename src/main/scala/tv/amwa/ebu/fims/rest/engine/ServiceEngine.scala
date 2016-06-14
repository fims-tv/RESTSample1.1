/* Copyright 2013 Advanced Media Workflow Association and European Broadcasting Union

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package tv.amwa.ebu.fims.rest.engine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.UUID
import tv.amwa.ebu.fims.rest.commons.Loggable
import tv.amwa.ebu.fims.rest.converter.StringConverters._
import tv.amwa.ebu.fims.rest.converter.FimsString
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import tv.amwa.ebu.fims.rest.model.capture._
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfile
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfileType
import tv.amwa.ebu.fims.rest.model._
import tv.amwa.ebu.fims.rest.model.BMContent
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.model.ResourceID
import tv.amwa.ebu.fims.rest.model.ResourceType
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.actor.Props
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag


trait ServiceEngine[T <: ResourceType] {
  def deleteAll
  def delete(resourceID: ResourceID): Either[Throwable, Null]
  def listResources(skip: Option[Int], limit : Option[Int]) : Either[Throwable, List[T]]
  def countResources: Int
  def createResource(toCreate: T): Either[Throwable, T]
  def updateResource(existingID: ResourceID, toUpdate: T): Either[Throwable, T]
  def contains(resourceID: ResourceID) : Boolean
  def getResource(resourceID: ResourceID): Either[Throwable, T]
  def pause(resourceID: ResourceID): Either[Throwable, T] = Left(new IllegalArgumentException("Cannot pause as not a job resource or pause is not supported."))
  def resume(resourceID: ResourceID): Either[Throwable, T] = Left(new IllegalArgumentException("Cannot resume as not a job resource or pause and resume is not supported."))
  def restart(resourceID: ResourceID): Either[Throwable, T] = Left(new IllegalArgumentException("Cannot restart as not a job resource or restart is not supported."))
  def cancel(resourceID: ResourceID): Either[Throwable, T] = Left(new IllegalArgumentException("Cannot cancel as not a job resource or cancelation is not supported."))
  def stop(resourceID: ResourceID): Either[Throwable, T] = Left(new IllegalArgumentException("Cannot stop as not a job resource or stopping is not supported."))
  def purge = ()
  def resourceType(implicit tag: TypeTag[T]) = tag
}

class MockCaptureEngine(val endpoint: String) extends ServiceEngine[CaptureJobType] with Loggable {
  import collection.JavaConverters._
  private val resources = new ConcurrentHashMap[String,CaptureJobType].asScala
  private val jobs = new ConcurrentHashMap[String,ActorRef].asScala
  val system = ActorSystem("mockfims")
  private[this] var jobCounter = 0
  
  override def deleteAll = { resources.clear; jobs.values.foreach(_ ! PoisonPill) }
  override def delete(resourceID: ResourceID) = {
    jobs.get(resourceID.toString).foreach{_ ! PoisonPill}
    resources.remove(resourceID.toString).map{x => Right(null)}.getOrElse(
      Left(new NotFoundException("On delete, a capture job with identifier " + resourceID.toString + " was not found in the in memory store."))) 
  }
  // TODO ordering in a list
  override def listResources(skip: Option[Int], limit:Option[Int]) = {
    try { Right(resources.drop(skip.getOrElse(0)).take(limit.getOrElse(Integer.MAX_VALUE)).map{x => enrichJob(x._2)}.toList) }
    catch { case e: Exception => Left(e) }
  } 
  override def contains(resourceID : ResourceID) = resources.contains(resourceID)
  override def getResource(resourceID : ResourceID) = {
    resources.get(resourceID.toString).toRight(new NotFoundException("Capture job with identifier " + resourceID.toString + " was not found in the in memory store."))
      .fold(x => Left(x), x => Right(enrichJob(x)))
  }
  override def createResource(resource : CaptureJobType) = {
    logger.info("Creating capture job {}.", resource)
    if (resources.contains(resource.resourceID.toString)) Left(new ItemAlreadyExistsException("A capture job with identifier " + resource.resourceID.toString + " already exists."))
    else try { 
      resource.checkOnCreation
      val createdResource = resource match {
        case CaptureJob(rp, bp, sp) => stripJobOnCreation(CaptureJob(rp.next.copy(location = Some(new java.net.URI(endpoint + "/api/job/" + FimsString.write(rp.resourceID)))), 
            bp.copy(status = Some(JobStatusType.Queued), statusDescription = Some("Capture job added to queue.")), sp))
        case _ => throw new ClassCastException("On capture job creation, deserialized value was not a Capture Job.")
      }
      resources.put(createdResource.resourceID.toString, createdResource) 
      jobs.put(resource.resourceID.toString, system.actorOf(Props(new MockCaptureJobActor(this, createdResource)), "capture" + jobs.size))
      Right(enrichJob(createdResource)) 
    } 
    catch { case e: Exception => Left(e) }
  }
  override def updateResource(existingID: ResourceID, update: CaptureJobType) = {
    logger.info("Updating capture job {}.", update)
    getResource(existingID).fold(
        e => Left(e), 
        existing => 
          try { val updatedJob = updateJob(existing, update); resources.put(existingID, updatedJob); Right(enrichJob(updatedJob))} 
          catch { case f: Exception => Left(f)}
      )}
  override def countResources = resources.size
  override def pause(resourceID: ResourceID) = {
    (for {
        resource <- resources.get(resourceID.toString)
        actor <- jobs.get(resourceID.toString) 
        if (resource.status.getOrElse(JobStatusType.Unknown) == JobStatusType.Running)}
      yield {
        actor ! Pause
        resource}).toRight(new IllegalStateException("Cannot pause a job other than one that is running."))
  }
  override def resume(resourceID: ResourceID) = {
    (for {
        resource <- resources.get(resourceID.toString)
        actor <- jobs.get(resourceID.toString) 
        if (resource.status.getOrElse(JobStatusType.Unknown) == JobStatusType.Paused)}
      yield {
        actor ! Resume
        resource}).toRight(new IllegalStateException("Cannot resume a job other than one that is paused."))
  }
  override def cancel(resourceID: ResourceID) = {
    (for {
        resource <- resources.get(resourceID.toString)
        actor <- jobs.get(resourceID.toString) 
        if (resource.status match { case Some(JobStatusType.Running) | Some(JobStatusType.Queued) => true ; case _ => false}) }
      yield {
        actor ! Cancel
        resource}).toRight(new IllegalStateException("Cannot cancel a job other than one that is running or queued."))
  }
  override def stop(resourceID: ResourceID) = {
    (for {
        resource <- resources.get(resourceID.toString)
        actor <- jobs.get(resourceID.toString) 
        if (resource.status.getOrElse(JobStatusType.Unknown) == JobStatusType.Running)
        if (resource.stopProcess match { case Some(StopProcessByOpenEnd(_, _)) => true; case _ => false }) }
      yield {
        actor ! ServiceStopEvent
        resource}).toRight(new IllegalStateException("Cannot stop a job other than one that is running and open ended."))
  }
  override def purge = {
    resources.retain((_, job) => (job.status.getOrElse(JobStatusType.Unknown)) match { 
      case JobStatusType.Canceled | JobStatusType.Stopped | JobStatusType.Completed => false; case _ => true })
  }
  private def updateJob(existing: CaptureJobType, updated: CaptureJobType): CaptureJobType = {
    if (existing.resourceID != updated.resourceID) throw new IllegalArgumentException("On update, resource identiiers do not match.")
    require(updated.status.isEmpty,"The status property is managed by the job.")
    existing match {
      case CaptureJob(rp, bp, sp) => CaptureJob(
          rp.next.copy(
            notifyAt = updated.notifyAt.orElse(rp.notifyAt)), 
          bp.copy(
            priority = updated.priority.orElse(bp.priority)), 
          sp.copy(
            stopProcess = updated.stopProcess.orElse(sp.stopProcess),
            startProcess = updated.startProcess.orElse(sp.startProcess)))
      case _ => throw new ClassCastException("On capture job update, deserialized value was not a Capture Job.")
    }
  }
  private def isBMContentEmpty(content: BMContentType) =
    content == BMContent(ResourceParameters(content.resourceID,content.revisionID), Nil, Nil)
  private lazy val emptyBaseProfileParameters = BaseProfileParameters()
  private lazy val emptyCaptureProfileParameters = CaptureProfileParameters()
  private def isProfileEmpty(profile: CaptureProfileType) =
    profile == CaptureProfile(ResourceParameters(profile.resourceID,profile.revisionID), emptyBaseProfileParameters, emptyCaptureProfileParameters)
  private def stripJobOnCreation(fat: CaptureJobType) = {
    CaptureJob(fat.resourceParameters, fat.baseParameters.copy(
        bmObjects = fat.baseParameters.bmObjects.map{case BMObject(rp, bmcs) => BMObject(rp, bmcs.map{bmc => 
          if (!isBMContentEmpty(bmc)) MemoryStoreOf.repository.createResource(bmc).fold(
            x => bmc, x => BMContent(x.resourceParameters, Nil, Nil))
          else bmc})}), fat.serviceParameters.copy(profiles = fat.serviceParameters.profiles.map{cp =>
            if (!isProfileEmpty(cp)) CaptureProfile(cp.resourceParameters, emptyBaseProfileParameters, emptyCaptureProfileParameters)
            else cp}))
  }
  private def enrichJob(thin: CaptureJobType) = {
    CaptureJob(thin.resourceParameters, thin.baseParameters.copy(
        bmObjects = thin.baseParameters.bmObjects.map{case BMObject(rp, bmcs) => BMObject(rp, bmcs.map{bmc =>
          if (isBMContentEmpty(bmc)) MemoryStoreOf.repository.getResource(bmc.resourceID.toString).fold(x => bmc, x => x)
          else bmc})}),
        thin.serviceParameters.copy(profiles = thin.serviceParameters.profiles.map{cp =>
          if (isProfileEmpty(cp)) MemoryStoreOf.profileStore.getResource(cp.resourceID.toString).fold(x => cp, x => x)
          else cp}))
  }
  private[engine] def internalUpdate(update: CaptureJobType) = {
    resources.put(update.resourceID.toString, update)
    update
  }
}

class MockRepositoryEngine(val endpoint: String) extends ServiceEngine[BMContentType] with Loggable {
  import collection.JavaConverters._
  private val resources = new ConcurrentHashMap[String,BMContentType].asScala
  
  override def deleteAll = resources.clear
  override def delete(resourceID: ResourceID) = resources.remove(resourceID).map{x => Right(null)}.getOrElse(
      Left(new NotFoundException("On delete, content with identifier " + resourceID.toString + " was not found in the in memory store."))) 
  override def listResources(skip: Option[Int], limit:Option[Int]) = {
    try { Right(resources.drop(skip.getOrElse(0)).take(limit.getOrElse(Integer.MAX_VALUE)).map{_._2}.toList) }
    catch { case e : Exception => Left(e) }
  } 
  override def contains(resourceID : ResourceID) = resources.contains(resourceID)
  override def getResource(resourceID : ResourceID) = {
    resources.get(resourceID.toString).toRight(new NotFoundException("BMContent with identifier " + resourceID.toString + " was not found in the in memory store."))
  }
  override def createResource(resource : BMContentType) = {
    logger.info("Creating content resource {}.", resource)
    if (resources.contains(resource.resourceID.toString)) Left(new ItemAlreadyExistsException("BMContent with identifier " + resource.resourceID.toString + " already exists."))
    else try { 
      val createdResource = resource match {
        case BMContent(rp, formats, descriptions, status) => 
          BMContent(rp.next.copy(location = Some(new java.net.URI(endpoint + "/api/content/" + FimsString.write(rp.resourceID)))), formats, descriptions, status)
        case _ => throw new ClassCastException("On content creation, deserialized value was not a BMContent instance.")
      }
      resources.put(createdResource.resourceID.toString, createdResource) 
      Right(createdResource) 
    } 
    catch { case e : Exception => Left(e) }
  }
  override def updateResource(existingID: ResourceID, update : BMContentType) = {
    logger.info("Updating content {}.", update)
    getResource(existingID) match {
      case Left(e) => Left(e)
      case Right(BMContent(rp, formats, _, status)) =>
        val updatedContent = BMContent(rp.next, formats, update.descriptions, status)
        resources.put(existingID.toString, updatedContent)
        Right(updatedContent) 
      case Right(other) => Right(other)
    }}
  override def countResources = resources.size
  override def purge = {
    val jobs = MemoryStoreOf.jobEngine.listResources(None, None).fold(x => Nil, x => x)
    val referencedIDs = jobs.flatMap{_.bmObjects.flatMap{_.bmContents.map{x => x}}}.map{_.resourceID}.toSet
    resources.retain((_, value) => referencedIDs.contains(value.resourceID))
  }
}

// TODO make this client specific
object ProfileEngine extends ServiceEngine[CaptureProfileType] {
  val nativeProfileUUID = UUID.fromString("7913953e-6155-423f-90bb-d3e60db57291")
  val lbrProfileUUID = UUID.fromString("b1ff36db-cd1d-4e5e-b6db-1571e5e239c2")
  val hbrProfileUUID = UUID.fromString("30bdb5ca-66e5-4b7a-b2fd-23f9569e82e2")
  private val baseVideoFormat = VideoFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Nil,
      Length(1280, "pixels"), Length(720, "pixels"),
      Rational(60, 1000, 1001), Rational(1, 16, 9),
      Codec("H.264"),
      BMTrack(UUIDBasedResourceID(UUID.randomUUID), None, None, None, "main video", "en-us"),
      100000000l, tv.amwa.ebu.fims.rest.model.Constant, 720, Progressive, None, false) 
  private val baseAudioFormat = AudioFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Nil,
      48000.0,
      Codec("PCM"),
      None,
      List(BMTrack(UUIDBasedResourceID(UUID.randomUUID), None, None, None, "left audio", "en-us"),
          BMTrack(UUIDBasedResourceID(UUID.randomUUID), None, None, None, "right audio", "en-us")),
      2, 1536000l,  tv.amwa.ebu.fims.rest.model.Constant, 16, IntegerType)      
  private val containerFormat = ContainerFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Nil,
      ContainerFormatParameters("AS-02", None, None, "http://www.amwa.tv"))

  val nativeProfile = CaptureProfile(ResourceParameters(nativeProfileUUID).next, BaseProfileParameters(), 
      CaptureProfileParameters(
          TransformAtom(baseVideoFormat.copy(videoEncoding = Codec("MPEG-2"), bitRate = 35000000l), 
              baseAudioFormat, containerFormat), 
          List(TransferAtom("urn:x-quantel:destination:native"))))
  val lbrProfile = CaptureProfile(ResourceParameters(lbrProfileUUID).next, BaseProfileParameters(), 
      CaptureProfileParameters(
          TransformAtom(baseVideoFormat.copy(bitRate = 8000000l), 
              baseAudioFormat.copy(audioEncoding = Codec("AAC"), bitRate = 128000l), containerFormat), 
          List(TransferAtom("urn:x-quantel:destination:lbr"))))
  val hbrProfile = CaptureProfile(ResourceParameters(hbrProfileUUID).next, BaseProfileParameters(), CaptureProfileParameters(
          TransformAtom(baseVideoFormat.copy(bitRate = 40000000l),
              baseAudioFormat, containerFormat),
          List(TransferAtom("urn:x-quantel:destination:hbr"))))
  val notImplemented = new NotImplementedException()
  override def deleteAll = {}
  override def delete(resourceID: ResourceID): Either[Throwable, Null] = Left(notImplemented)
  override def listResources(skip: Option[Int], limit : Option[Int]) : Either[Throwable, List[CaptureProfileType]] = Left(notImplemented)
  override def countResources: Int = 3
  override def createResource(toCreate: CaptureProfileType): Either[Throwable, CaptureProfileType] = Left(notImplemented)
  override def updateResource(existingID: ResourceID, toUpdate: CaptureProfileType): Either[Throwable, CaptureProfileType] = Left(notImplemented)
  override def contains(resourceID: ResourceID) : Boolean = 
    resourceID == nativeProfile.resourceID || resourceID == lbrProfile.resourceID || resourceID == hbrProfile.resourceID
  override  def getResource(resourceID: ResourceID): Either[Throwable, CaptureProfileType] = {
    resourceID match {
      case nativeProfile.resourceID => Right(nativeProfile)
      case lbrProfile.resourceID => Right(lbrProfile)
      case hbrProfile.resourceID => Right(hbrProfile)
      case _ => Left(new IllegalArgumentException("Could not resolve reference to profile."))
    }
  }
}

object MemoryStoreOf {
//  val bmoStore = new MemoryStoreOf[BMObjectType]
  val repository = new MockRepositoryEngine("http://localhost:9000")
  val jobEngine = new MockCaptureEngine("http://localhost:9000")
//  val queueStore = new MemoryStoreOf[QueueType]
//  val serviceStore = new MemoryStoreOf[ServiceType]
  val profileStore = ProfileEngine
//  val formatStore = new MemoryStoreOf[FormatType]
//  val bmContentFormatStore = new MemoryStoreOf[BMContentFormatType]
//  val bmEssenceLocatorStore = new MemoryStoreOf[BMEssenceLocatorType]
//  val descriptionStore = new MemoryStoreOf[DescriptionTypeType]
}

