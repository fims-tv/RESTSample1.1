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

trait BMContentType extends ResourceType {
  val bmContentFormats: Seq[BMContentFormatType]
  val descriptions: Seq[DescriptionType]
  val status: Option[BMContentStatusType]
}

case class BMContent(
    resourceParameters: ResourceParameters,
    bmContentFormats: Seq[BMContentFormatType] = Nil,
    descriptions: Seq[DescriptionType] = Nil,
    status: Option[BMContentStatusType] = None) extends BMContentType
 
trait BMContentStatusType extends ResourceType {
  val resourceParameters: ResourceParameters
  val status: BMStatusType
  val subStatus: Option[BMCustomStatusExtensionType]
}

case class BMContentStatus(
    resourceParameters: ResourceParameters,
    status: BMStatusType,
    subStatus: Option[BMCustomStatusExtensionType]) extends BMContentStatusType

sealed trait BMStatusType

object BMStatusType {
  def fromString(value: String): BMStatusType = value match {
    case "new" => New
    case "online" => Online
    case "offline" => Offline
    case "removed" => Removed
    case "purged" => Purged
    case "invalid" => Invalid
    case "processing" => Processing
    case _ => throw new IllegalArgumentException("String value '" + value + "' does not match enumeration for BM status type.")
  }
  case object New extends BMStatusType { override def toString = "new" }
  case object Online extends BMStatusType { override def toString = "online" }
  case object Offline extends BMStatusType { override def toString = "offline" }
  case object Removed extends BMStatusType { override def toString = "removed" }
  case object Purged extends BMStatusType { override def toString = "purged" }
  case object Invalid extends BMStatusType { override def toString = "invalid" }
  case object Processing extends BMStatusType { override def toString = "processing" }
}

trait BMCustomStatusExtensionType extends ResourceType {
  val code: String
  val description: Option[String]
}

case class BMCustomStatusExtension(
    resourceParameters: ResourceParameters,
    code: String,
    description: Option[String]) extends BMCustomStatusExtensionType

