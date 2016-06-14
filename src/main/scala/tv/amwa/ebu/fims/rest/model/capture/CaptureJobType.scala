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

package tv.amwa.ebu.fims.rest.model.capture

import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.model.StartProcessType
import tv.amwa.ebu.fims.rest.model.StopProcessType
import tv.amwa.ebu.fims.rest.model.ServiceJobParameters
import tv.amwa.ebu.fims.rest.model.Time
import tv.amwa.ebu.fims.rest.model.Duration
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StartProcessByTime
import tv.amwa.ebu.fims.rest.model.StartProcessByTimeMark
import tv.amwa.ebu.fims.rest.model.Timecode
import tv.amwa.ebu.fims.rest.model.StopProcessByOpenEnd
import tv.amwa.ebu.fims.rest.model.StopProcessByServiceDefinedTime
import tv.amwa.ebu.fims.rest.model.StopProcessByTime
import tv.amwa.ebu.fims.rest.model.StartJobByNoWait
import tv.amwa.ebu.fims.rest.model.StartJobByTime
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessByTime
import tv.amwa.ebu.fims.rest.model.StopProcessByDuration
import tv.amwa.ebu.fims.rest.model.EditUnitNumber
import tv.amwa.ebu.fims.rest.model.StopProcessByTimeMark
import tv.amwa.ebu.fims.rest.model.EditUnitNumber
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StartProcessByTimeMark

trait CaptureJobType extends JobType {
  val serviceParameters: CaptureJobParameters
  lazy val profiles = serviceParameters.profiles
  lazy val startProcess = serviceParameters.startProcess
  lazy val stopProcess = serviceParameters.stopProcess
  lazy val sourceID = serviceParameters.sourceID
  lazy val sourceType = serviceParameters.sourceType
  lazy val inPoint = serviceParameters.inPoint
  lazy val outPoint = serviceParameters.outPoint
  lazy val splitOnTCBreak = serviceParameters.splitOnTCBreak
  private def checkStartAndStop = { // TODO test this properly
    startProcess.getOrElse(StartProcessByNoWait) match {
      case StartProcessByTime(processTime) => 
        startJob.getOrElse(StartJobByNoWait) match {
          case StartJobByTime(jobTime) => require(processTime >= jobTime, "A capture job cannot have a process start time after a job start time.")
          case _ => require(System.currentTimeMillis >= processTime, "A capture job with the start process parameter defined by time cannot start in the past.")
        }
      case _ => ()
    }
    stopProcess.getOrElse(StopProcessByOpenEnd) match {
      case StopProcessByTime(stopTime) => 
        startProcess.getOrElse(StartProcessByNoWait) match {
          case StartProcessByTime(startTime) => require(stopTime < startTime, "A capture job with the stop process parameter defined by time cannot end before it is scheduled to start.")
          case _ => ()
        }
      case StopProcessByDuration(duration) => duration.specifier match {
        case EditUnitNumber(unit, _, _, _) => require(unit >= 0, "A capture job with the stop process parameter defined by an edit unit duration must be a non-negative edit unit count.")
        case _ => ()
      }
      case StopProcessByTimeMark(timeMark) => timeMark.specifier match {
        case EditUnitNumber(stopUnit, stopRate, stopNum, stopDenom) => startProcess.getOrElse(StartProcessByNoWait) match {
          case StartProcessByTimeMark(Time(EditUnitNumber(startUnit, startRate, startNum, startDenom))) 
           if ((stopRate, stopNum, stopDenom) == (startRate, startNum, startDenom)) => 
             require(stopUnit > startUnit, "A capture job with the stop process parameter defined by an time since stream start edit unit number count must be after the start process count.")
          case _ => require(stopUnit > 0, "A capture job with the stop process parameter defined by an edit unit count from stream start must be a non-negative edit unit count.") 
        }
        case _ => ()
      }
      case _ => ()
    }
  }
  override def checkOnCreation = {
    super.checkOnCreation
    require(profiles.length > 0, "A capture job request must contain at least one profile.")
    require(startProcess.isDefined, "A capture job request must contain details of how to start the capture process.")
    require(stopProcess.isDefined, "A capture job request must contain details of how to stop the capture process.")
    require(sourceID.isDefined, "A capture job request must contain details of the capture source identifier.")
    require(sourceType.isDefined, "A capture job request must define whether the source is controllable or not.")
    checkStartAndStop
  }
  override def checkOnUpdate = {
    super.checkOnUpdate
    checkStartAndStop
  }
}

case class CaptureJob(
    resourceParameters: ResourceParameters,
    baseParameters: BaseJobParameters,
    serviceParameters: CaptureJobParameters) extends CaptureJobType

case class CaptureJobParameters(
  profiles: Seq[CaptureProfileType] = Nil,
  startProcess: Option[StartProcessType] = None,
  stopProcess: Option[StopProcessType] = None,
  sourceID: Option[java.net.URI] = None, 
  sourceType: Option[SourceType] = None,
  inPoint: Option[SourceInPointType] = None,
  outPoint: Option[SourceOutPointType] = None,
  splitOnTCBreak: Option[Boolean] = None) extends ServiceJobParameters
  
sealed trait SourceType

object SourceType {
  def fromString(value: String): SourceType = value match {
    case "controllable" => Controllable
    case "uncontrolled" => Uncontrolled
  }
}

case object Controllable extends SourceType { override def toString = "controllable" }
case object Uncontrolled extends SourceType { override def toString = "uncontrolled" }

/** Kinds of source "In" point used as a parameter for a capture process.
  */
sealed trait SourceInPointType 

/** The current position of the capture source used as the source "In" point.
  */
case class SourceInPointByCurrent() extends SourceInPointType

/** Source "In" point specified by a time mark in the capture source essence.
  */
case class SourceInPointByTimeMark(
    timeMark: Time) extends SourceInPointType

/** Source "In" point is at the beginning of the capture source, such as VTR tape.
  */
case class SourceInPointByBeginning() extends SourceInPointType


/** Kinds of source "Out" point used as a parameter for a capture process.
  */
sealed trait SourceOutPointType 

/** Source "Out" point specified by the total duration of material to capture.
  */
case class SourceOutPointByDuration(
    duration: Duration) extends SourceOutPointType


/** Source "Out" point specified by a position in the capture source essence.
  */
case class SourceOutPointByTimeMark(
    timeMark: Time) extends SourceOutPointType

/** Source "Out" point is not specified and capture continues indefinitely until stop command (manageJobRequest) is received.
  */
case class SourceOutPointByOpenEnd() extends SourceOutPointType


/** Source "Out" point is the end of the capture source, such as VTR tape.
  */
case class SourceOutPointByEnd() extends SourceOutPointType

