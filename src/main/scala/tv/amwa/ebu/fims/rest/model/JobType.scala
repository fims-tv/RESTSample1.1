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

package tv.amwa.ebu.fims.rest.model

trait ServiceJobParameters

/** Describes a job, which is extended in
 *  each service schema. RequiredSupported job commands
 */
trait JobType extends ResourceType {
  val baseParameters: BaseJobParameters
  val serviceParameters: ServiceJobParameters
  lazy val status = baseParameters.status
  lazy val statusDescription = baseParameters.statusDescription
  lazy val serviceProviderJobID = baseParameters.serviceProviderJobID
  lazy val queueReference = baseParameters.queueReference
  lazy val tasks = baseParameters.tasks
  lazy val operationName = baseParameters.operationName
  lazy val bmObjects = baseParameters.bmObjects
  lazy val priority = baseParameters.priority
  lazy val startJob = baseParameters.startJob
  lazy val finishBefore = baseParameters.finishBefore
  lazy val estimatedCompletionDuration = baseParameters.estimatedCompletionDuration
  lazy val currentQueuePosition = baseParameters.currentQueuePosition
  lazy val jobStartTime = baseParameters.jobStartedTime
  lazy val jobElapsedTime = baseParameters.jobElapsedTime
  lazy val jobCompletedTime = baseParameters.jobCompletedTime
  lazy val processed = baseParameters.processed
  def checkOnCreation = {
    checkRequest
    require(startJob.isDefined, "Job request must define details of the start job time.")
    require(startJob.getOrElse(StartJobByNoWait) match {
      case StartJobByNoWait | StartJobByLatest => true
      case StartJobByTime(time) => (time - System.currentTimeMillis) >= 0
      }, "Start time for a job request must be in the future, or use start job by latest.")
  }
  def checkOnUpdate = {
    checkRequest
  }
  private def checkRequest = {
    require(status.isEmpty, "Job request cannot define a status.")
    require(statusDescription.isEmpty, "Job request cannot define a status description.")
    require(queueReference.isEmpty, "Job request cannot define a queue reference.")
    require(serviceProviderJobID.isEmpty, "Job request cannot define a service provider job id.")
    require(tasks.isEmpty, "Job request cannot define sub tasks")
    require(operationName.isEmpty, "Job request cannot define an operation name.")
    require(estimatedCompletionDuration.isEmpty, "Job request cannot define an estimated completion duration.")
    require(currentQueuePosition.isEmpty, "Job request cannot define a current queue position.")
    require(jobStartTime.isEmpty, "Job request cannot define a job start time.")
    require(jobElapsedTime.isEmpty, "Job request cannot define a job elapsed time.")
    require(jobCompletedTime.isEmpty, "Job request cannot define a job completed time.")
    require(processed.isEmpty, "Job request cannot define job processing details.")    
  }
}

case class BaseJobParameters(
  status: Option[JobStatusType] = None,
  statusDescription: Option[String] = None,
  serviceProviderJobID: Option[String] = None,
  queueReference: Option[QueueType] = None,
  tasks: Seq[JobType] = Nil,
  operationName: Option[String] = None,
  bmObjects: Seq[BMObjectType] = Nil,
  priority: Option[PriorityType] = None,
  startJob: Option[StartJobType] = None,
  finishBefore: Option[Long] = None,
  estimatedCompletionDuration: Option[Long] = None,
  currentQueuePosition: Option[Int] = None,
  jobStartedTime: Option[Long] = None,
  jobElapsedTime: Option[Long] = None,
  jobCompletedTime: Option[Long] = None,
  processed: Option[ProcessedInfoType] = None) {
  
  require(currentQueuePosition.map(_ >= 0).getOrElse(true))
}
  
sealed trait JobStatusType

object JobStatusType {
  def fromString(value: String): JobStatusType = value match {
    case "new" => New
    case "scheduled" => Scheduled
    case "queued" => Queued
    case "running" => Running
    case "paused" => Paused
    case "completed" => Completed
    case "canceled" => Canceled
    case "stopped" => Stopped
    case "failed" => Failed
    case "cleaned" => Cleaned
    case "unknown" => Unknown
    case _ => throw new IllegalArgumentException("String value '" + value + "' does not match a job status enumeration value.")
  }
  
  case object New extends JobStatusType { override def toString = "new" }
  case object Scheduled extends JobStatusType { override def toString = "scheduled" }
  case object Queued extends JobStatusType { override def toString = "queued" }
  case object Running extends JobStatusType { override def toString = "running" }
  case object Paused extends JobStatusType { override def toString = "paused" }
  case object Completed extends JobStatusType { override def toString = "completed" }
  case object Canceled extends JobStatusType { override def toString = "canceled" }
  case object Stopped extends JobStatusType { override def toString = "stopped" }
  case object Failed extends JobStatusType { override def toString = "failed" }
  case object Cleaned extends JobStatusType { override def toString = "cleaned" }
  case object Unknown extends JobStatusType { override def toString = "unknown" }
}

trait PriorityType

object PriorityType {
  def fromString(value: String): PriorityType = value match {
    case "low" => Low
    case "medium" => Medium
    case "high" => High
    case "urgent" => Urgent
    case "immediate" => Immediate

  }
}

case object Low extends PriorityType { override def toString = "low" }
case object Medium extends PriorityType { override def toString = "medium" }
case object High extends PriorityType { override def toString = "high" }
case object Urgent extends PriorityType { override def toString = "urgent" }
case object Immediate extends PriorityType { override def toString = "immediate" }

/** Statistics on the amount of information
  * processed so far. For example in terms of bytes or frames processed.RequiredThe type of processed
  * information provided.
  */
trait ProcessedInfoType {
  val percentageProcessedCompleted: Int
  require(percentageProcessedCompleted >= 0)
  require(percentageProcessedCompleted <= 100)
}


/** Statistics on number of bytes processed.
*/
case class ProcessedInfoByBytes(
    val percentageProcessedCompleted: Int,
    processedBytesCount: Long) extends ProcessedInfoType {
  require(processedBytesCount >= 0l)
}


/** Statistics on number of frames processed.
*/
case class ProcessedInfoByFrames(
    val percentageProcessedCompleted: Int,
    processedFramesCount: Long) extends ProcessedInfoType {
  require(processedFramesCount >= 0l)
}
  
/** Kinds of time when a job is required to start.One of  the following types shall be used.
 *  StartJobByNoWaitType shall be supported.RequiredSupported start job types
 */
sealed trait StartJobType

/** Start a job as soon as possible.The job shall start without waiting
  *  for a specific time or event.
  *  This type shall be supported.
  */
case class StartJobByNoWait() extends StartJobType


/** Provides a time when the job should start.
  */
case class StartJobByTime(
    time: Long) extends StartJobType


/** Start the job as close as possible to the defined start process time.
  */
case class StartJobByLatest() extends StartJobType

/** Kinds of time, time code, or event based information used to start a real-time process such as a capture process.
  * One of the following types shall be used. StartProcessByNoWaitType and StartProcessByServiceDefinedTimeType shall 
  * be supported.
  */
sealed trait StartProcessType

sealed trait StartProcessInfo {
  val actualStartTime: Option[Long]
  val actualStartMark: Option[Time]
}

/** Start a real-time process with no initial wait.The process shall start without waiting for a specific time or event.
  */
case class StartProcessByNoWait(
  actualStartTime: Option[Long] = None,
  actualStartMark: Option[Time] = None) extends StartProcessType with StartProcessInfo

/** Provides the time when a real-time process should start.
  */
case class StartProcessByTime(
    time: Long) extends StartProcessType

/** Provides a video or audio stream time mark value to indicate the required start of a real-time process. 
*/
case class StartProcessByTimeMark(
    timeMark: Time) extends StartProcessType


/** A service-specific time mechanism specifies when a process is required to start.The service shall wait for 
  * an event to start the process. The actual event depends on the service implementation.
  * A service shall support this type.
  */
case class StartProcessByServiceDefinedTime(
  actualStartTime: Option[Long] = None,
  actualStartMark: Option[Time] = None) extends StartProcessType with StartProcessInfo


/** Kind of time, time code, or event information used to stop a real-time process, such as a capture process.
  * One of the following types shall be used. StopProcessByServiceDefinedTimeType shall be supported.
  */
sealed trait StopProcessType 

sealed trait StopProcessInfo {
  val actualStopTime: Option[Long]
  val actualStopMark: Option[Time]
}

/** The time when a real-time process should stop.
  */
case class StopProcessByTime(
    time: Long) extends StopProcessType

/** Total duration of the real time process.A real time process shall stop once the specified duration has elapsed.
  */
case class StopProcessByDuration(
  duration: Duration) extends StopProcessType

/** A video or audio stream time reference when the real-time process stops. 
  */
case class StopProcessByTimeMark(
  timeMark: Time) extends StopProcessType

/** A service-specific time mechanism specifies when a process is required to stop. The service shall wait for an event 
  * to stop the process. The actual event depends on the service implementation. A service shall support this type.
  */
case class StopProcessByServiceDefinedTime(
  actualStopTime: Option[Long] = None,
  actualStopMark: Option[Time] = None) extends StopProcessType with StopProcessInfo


/** The real-time process is to continue indefinitely until a stop command (manageJobRequest) is received.
  */
case class StopProcessByOpenEnd(
  actualStopTime: Option[Long] = None,
  actualStopMark: Option[Time] = None) extends StopProcessType with StopProcessInfo



