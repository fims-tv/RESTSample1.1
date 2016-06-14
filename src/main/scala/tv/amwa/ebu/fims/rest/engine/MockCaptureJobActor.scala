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
import tv.amwa.ebu.fims.rest.model.ResourceID
import akka.actor.Actor
import akka.event.LoggingReceive
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.model.StartJobByNoWait
import tv.amwa.ebu.fims.rest.model.StartJobByLatest
import tv.amwa.ebu.fims.rest.model.StartJobByTime
import scala.concurrent.duration._
import tv.amwa.ebu.fims.rest.model.StartProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StartProcessByTime
import tv.amwa.ebu.fims.rest.model.StartProcessByTimeMark
import tv.amwa.ebu.fims.rest.model.Timecode
import tv.amwa.ebu.fims.rest.model.NormalPlayTime
import tv.amwa.ebu.fims.rest.model.EditUnitNumber
import org.joda.time.DateTime
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.commons.Loggable
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.Client
import tv.amwa.ebu.fims.rest.rest.message.JobXMLMessageBodyWriter
import tv.amwa.ebu.fims.rest.rest.message.HTTPErrorXMLMessageBodyWriter
import scala.collection.JavaConverters.setAsJavaSetConverter
import tv.amwa.ebu.fims.rest.model.ProcessedInfoByBytes
import tv.amwa.ebu.fims.rest.model.StopProcessByDuration
import tv.amwa.ebu.fims.rest.model.StopProcessByOpenEnd
import tv.amwa.ebu.fims.rest.model.StopProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StopProcessByTime
import tv.amwa.ebu.fims.rest.model.StopProcessByTimeMark
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessType
import akka.actor.Props
import tv.amwa.ebu.fims.rest.model.ProcessedInfoByBytes
import tv.amwa.ebu.fims.rest.model.StopProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StopProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StopProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StopProcessByOpenEnd
import tv.amwa.ebu.fims.rest.model.StopProcessType
import scala.concurrent.ExecutionContext.Implicits.global
import tv.amwa.ebu.fims.rest.model.JobStatusType

class MockCaptureJobActor(
    engine: MockCaptureEngine,
    captureJob: CaptureJobType) extends Actor with Loggable {
  private val resourceID = captureJob.resourceParameters.resourceID
  private val timecodeRegex = """([0-9][0-9])\:([0-5][0-9])\:([0-5][0-9])([\:\;])([0-9][0-9])(\.[0-1])?""".r
  private var startProcessCalculation: Option[Long] = None
  private var finishTime: Long = 0l
  private val notifier = context.actorOf(Props(new Notifier), "notify")
  private var paused = false
  
  def timecodeToDuration(timecode: String) : Long = {
    timecodeRegex.findFirstMatchIn(timecode).get.subgroups match {
      case hour :: minute :: second :: tcType :: frame :: field :: Nil => {
        hour.toInt * 3600000 + minute.toInt * 60000 + second.toInt * 1000 + 
        (if (tcType == ";") frame.toDouble / 30000 * 1001000 else frame.toDouble / 25 * 1000).toLong
      }
      case _ => 0l
    }
  }
  def timecodeToTime(timecode: String) : Long = { // TODO consider the clock change
    val todayMidnight = DateTime.now.toDateMidnight.getMillis
    val duration = timecodeToDuration(timecode)
    if (todayMidnight + duration < System.currentTimeMillis) todayMidnight + duration + 86400000l else todayMidnight + duration
  }
 def preRoleProcess(desiredTime: Long) = {
	  ((desiredTime - System.currentTimeMillis) milliseconds) match {
	    case gap if (gap >= 5.seconds) => gap
	    case _ => Duration.Zero
	  }
  }
  
  override def preStart = {
    captureJob.startJob.getOrElse(StartJobByNoWait) match {
      case StartJobByNoWait => self ! StartJob
      case StartJobByLatest => captureJob.startProcess.getOrElse(StartProcessByServiceDefinedTime) match {
        case StartProcessByNoWait => self ! StartJob
        case StartProcessByServiceDefinedTime => ()
        case StartProcessByTime(time) => 
          context.system.scheduler.scheduleOnce(preRoleProcess(time)){self ! StartJob}
        case StartProcessByTimeMark(timeMark) => timeMark.specifier match {
          case Timecode(tc) =>
            context.system.scheduler.scheduleOnce(preRoleProcess(timecodeToTime(tc))){self ! StartJob}
          case NormalPlayTime(time) =>
            context.system.scheduler.scheduleOnce(preRoleProcess(time)){self ! StartJob}
          case EditUnitNumber(_, _, _, _) => self ! StartJob // Assumes you want to start the job and count edit units
        }
      }
      case StartJobByTime(time) => 
        context.system.scheduler.scheduleOnce((time - System.currentTimeMillis) milliseconds){self ! StartJob}
    }
  }
  
  override def receive: Receive = LoggingReceive {
    case StartJob => 
      logger.info("Starting capture job " + resourceID.toString)
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(status = Some(JobStatusType.Running), statusDescription = Some("Running"), jobStartedTime = Some(System.currentTimeMillis)), sp)
      })
      modifiedJob.startProcess.getOrElse(StartProcessByServiceDefinedTime) match {
        case StartProcessByNoWait(_, _) => self ! StartProcess
        case StartProcessByServiceDefinedTime(_, _) => ()
        case StartProcessByTime(time) => 
          context.system.scheduler.scheduleOnce(preRoleProcess(time)){self ! StartProcess}
        case StartProcessByTimeMark(timeMark) => timeMark.specifier match {
          case Timecode(tc) =>
            context.system.scheduler.scheduleOnce(preRoleProcess(timecodeToTime(tc))){self ! StartProcess}
          case NormalPlayTime(time) =>
            context.system.scheduler.scheduleOnce(preRoleProcess(time)){self ! StartProcess}
          case EditUnitNumber(editUnit, editRate, factorNumerator, factorDenominator) => 
            startProcessCalculation = Some(((editUnit * factorDenominator * 1000) / (factorNumerator * editRate)).toLong)
            context.system.scheduler.scheduleOnce(startProcessCalculation.get.milliseconds){self ! StartProcess}
        }
        notifier ! Notify(modifiedJob)
      }
      
    case StartProcess => // TODO enrich content with essence location and format information
      val currentTime = System.currentTimeMillis
      logger.info("Starting capture process for resource " + resourceID.toString)
      finishTime = captureJob.stopProcess.getOrElse(StopProcessByServiceDefinedTime) match {
        case StopProcessByDuration(duration) => 
          (duration.specifier match {
            case Timecode(tc) => timecodeToDuration(tc) 
            case NormalPlayTime(time) => time
            case EditUnitNumber(editUnit, editRate, factorNumerator, factorDenominator) => 
              ((editUnit * factorDenominator * 1000) / (factorNumerator * editRate)).toLong
            }) + currentTime
        case StopProcessByOpenEnd(_, _) => currentTime + 256 * 356 * 24 * 60 * 60 * 1000 // run for 256 years - Scala max is 292 years
        case StopProcessByServiceDefinedTime(_, _) => currentTime + 256 * 356 * 24 * 60 * 60 * 1000 // run for 256 years - Scala max is 292 years
        case StopProcessByTime(time) => time
        case StopProcessByTimeMark(timeMark) => timeMark.specifier match {
          case Timecode(tc) => timecodeToTime(tc)
          case NormalPlayTime(time) => time 
          case EditUnitNumber(editUnit, editRate, factorNumerator, factorDenominator) =>
           ((editUnit * factorDenominator * 1000) / (factorNumerator * editRate)).toLong - startProcessCalculation.getOrElse(0l)
        }
      }
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
                  status = Some(JobStatusType.Running), 
                  statusDescription = Some("Running and processing"), 
                  jobStartedTime = Some(bp.jobStartedTime.getOrElse(currentTime)),
                  jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime)),
                  estimatedCompletionDuration = (finishTime - currentTime) match { case d if d <= (86400000l) => Some(d); case _ => None },
                  processed = Some(ProcessedInfoByBytes(0, 0l))), 
              sp.copy(
                  startProcess = Some(sp.startProcess.getOrElse(StartProcessByServiceDefinedTime) match {
                    case StartProcessByServiceDefinedTime(_, _) => StartProcessByServiceDefinedTime(Some(System.currentTimeMillis)) // TODO support time mark
                    case StartProcessByNoWait(_, _) => StartProcessByNoWait(Some(System.currentTimeMillis))
                    case other : StartProcessType => other
                  })))
      })
      notifier ! Notify(modifiedJob)
      context.system.scheduler.scheduleOnce(((finishTime.max(86400000l) - currentTime)/100) milliseconds){self ! UpdateProgress}
      
    case UpdateProgress if !paused =>
      val currentTime = System.currentTimeMillis
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
              status = Some(JobStatusType.Running), 
              statusDescription = Some("Running and processing"), 
              jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime)),
              processed = Some(bp.processed match {
                case Some(ProcessedInfoByBytes(percent, _)) if percent < 100 => ProcessedInfoByBytes(percent + 1, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                case Some(ProcessedInfoByBytes(percent, _)) => ProcessedInfoByBytes(100, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                case None => ProcessedInfoByBytes(0, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
              })), sp)
      })
      val percentComplete = modifiedJob.processed.get.percentageProcessedCompleted
      if (percentComplete == 100) self ! Success
      else {
        if (percentComplete % 10 == 0) notifier ! Notify(modifiedJob)
        logger.info(percentComplete + " : " + ((finishTime.max(86400000l) - currentTime)/(100 - percentComplete)))
        context.system.scheduler.scheduleOnce(((finishTime.max(86400000l) - currentTime)/(100 - percentComplete)) milliseconds){self ! UpdateProgress}
      }

    case UpdateProgress if paused => 
      logger.debug("Update of capture job ignored as in paused state.")
    
    case ServiceStopEvent =>
      val currentTime = System.currentTimeMillis
      // Assume that state chart is checked synchronously
      val currentJob: CaptureJobType = engine.getResource(resourceID).right.get
//      currentJob.status match { 
//        case Some(Running) | Some(Paused) => 
          val modifiedJob = engine.internalUpdate(currentJob match {
             case CaptureJob(rp, bp, sp) => 
               CaptureJob(rp.next, bp.copy(
                 status = Some(JobStatusType.Stopped), 
                statusDescription = Some("Stopped"), 
                jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime)),
                jobCompletedTime = Some(currentTime),
                processed = Some(bp.processed match {
                  case Some(ProcessedInfoByBytes(percent, _)) => ProcessedInfoByBytes(100, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                  case _ => ProcessedInfoByBytes(100, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                })), sp)
          })
//        case _ => throw new IllegalStateException("Cannot stop a job in a " + currentJob.status.getOrElse(Queued).toString + " state.")
//      }
      
    case ServiceStartEvent =>
      self ! StartProcess
      
    case Success => 
      val currentTime = System.currentTimeMillis
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
                  status = Some(JobStatusType.Completed), 
                  statusDescription = Some("Completed"), 
                  jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime)),
                  jobCompletedTime = Some(currentTime)
              ), sp.copy(
                  stopProcess = Some(sp.stopProcess.getOrElse(StopProcessByServiceDefinedTime) match {
                    case StopProcessByServiceDefinedTime(_, _) => StopProcessByServiceDefinedTime(Some(currentTime))
                    case StopProcessByOpenEnd(_, _) => StopProcessByOpenEnd(Some(currentTime))
                    case other : StopProcessType => other
                  })
          	  ))
        })
      notifier ! Notify(modifiedJob)
    
    case Cancel =>
      val currentTime = System.currentTimeMillis
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
              status = Some(JobStatusType.Canceled), 
              statusDescription = Some("Canceled"), 
              jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime)),
              jobCompletedTime = Some(currentTime),
              processed = Some(bp.processed match {
                  case Some(ProcessedInfoByBytes(percent, _)) => ProcessedInfoByBytes(percent, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                  case _ => ProcessedInfoByBytes(0, 6250 * (currentTime - bp.jobStartedTime.getOrElse(currentTime)))
                })), sp)   
        })
      notifier ! Notify(modifiedJob)
    
    // FIXME simulated processed info byte count will go wrong after a pause
    case Pause =>
      paused = true
      val currentTime = System.currentTimeMillis
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
              status = Some(JobStatusType.Paused), 
              statusDescription = Some("Paused"), 
              jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime))
              ), sp)   
        }) 
      notifier ! Notify(modifiedJob)
      
    case Resume =>
      paused = false
      val currentTime = System.currentTimeMillis
      val modifiedJob = engine.internalUpdate(engine.getResource(resourceID).right.get match {
        case CaptureJob(rp, bp, sp) => 
          CaptureJob(rp.next, bp.copy(
              status = Some(JobStatusType.Running), 
              statusDescription = Some("Running and processing"), 
              jobElapsedTime = Some(currentTime - bp.jobStartedTime.getOrElse(currentTime))
              ), sp)   
        }) 
      notifier ! Notify(modifiedJob)
    
    // TODO faults and failures
  }
}

case object StartJob
case object StartProcess
case object StopProcess
case object UpdateProgress
case object ServiceStartEvent
case object ServiceStopEvent
case object Success
case object Pause
case object Resume
case object Cancel
case object Fault

case class Notify(job: CaptureJobType)

class Notifier extends Actor with Loggable {
  val client = {
    val config = new DefaultClientConfig
    config.getSingletons().addAll(Set(JobXMLMessageBodyWriter,HTTPErrorXMLMessageBodyWriter).asJava)
    Client.create(config)
  }

  def receive: Receive = LoggingReceive {
    case Notify(job) =>
      job.notifyAt.foreach{notify =>
        try {
          client.resource(notify.replyTo).post(job)
        } catch {case e: Exception => logger.error("Failed to post notification to reply to end point " + notify.replyTo + " for capture job " + job.resourceID.toString + ".", e)}
      }
  }
}