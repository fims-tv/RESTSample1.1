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

trait QueueType extends ResourceType {
  val status: Option[QueueStatusType]
  val statusDescription: Option[String]
  val length: Option[Int]
  val availability: Option[Boolean]
  val estimatedTotalCompletionDuration: Option[Long]
  val jobs: Seq[JobType]
  require(length.map{_ >= 0}.getOrElse(true))
}

case class Queue(
    resourceParameters: ResourceParameters,
    status: Option[QueueStatusType] = None,
    statusDescription: Option[String] = None,
    length: Option[Int] = None,
    availability: Option[Boolean] = None,
    estimatedTotalCompletionDuration: Option[Long] = None,
    jobs: Seq[JobType] = Nil) extends QueueType
    
sealed trait QueueStatusType

object QueueStatusType {
  def fromString(value: String): QueueStatusType = value match {
    case "started" => QueueStarted
    case "stopped" => QueueStopped
    case "locked" => QueueLocked
  }
}

case object QueueStarted extends QueueStatusType { override def toString = "started" }
case object QueueStopped extends QueueStatusType { override def toString = "stopped" }
case object QueueLocked extends QueueStatusType { override def toString = "locked" }
