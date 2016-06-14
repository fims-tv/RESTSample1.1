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
import scala.xml.Attribute

trait BMEssenceLocatorType extends ResourceType {
  val storageType: Option[Storage]
  val locatorInfo: Option[String]
  val containerMimeType: Option[ContainerMimeType]
  val status: Option[BMEssenceLocatorStatusType]
}

trait EssencePlaceholderLocatorType extends BMEssenceLocatorType 

case class EssencePlaceholderLocator(
    resourceParameters: ResourceParameters,
    storageType: Option[Storage] = None,
    locatorInfo: Option[String] = None,
    containerMimeType: Option[ContainerMimeType] = None,
    status: Option[BMEssenceLocatorStatusType] = None) extends EssencePlaceholderLocatorType

trait SimpleFileLocatorType extends BMEssenceLocatorType {
  val file: Option[java.net.URI]
}

/** Location of essence represented by a single file */
case class SimpleFileLocator(
    resourceParameters: ResourceParameters,
    storageType: Option[Storage] = None,
    locatorInfo: Option[String] = None,
    containerMimeType: Option[ContainerMimeType] = None,
    status: Option[BMEssenceLocatorStatusType] = None,
    file: Option[java.net.URI] = None) extends SimpleFileLocatorType

trait ListFileLocatorType extends BMEssenceLocatorType {
  val file: Seq[java.net.URI]
}
    
/** Location of essence represented by a list of files, such as the collection of multiple bitrate files 
 *  for HTTP Live Streaming. */
case class ListFileLocator(
    resourceParameters: ResourceParameters,
    storageType: Option[Storage] = None,
    locatorInfo: Option[String] = None,
    containerMimeType: Option[ContainerMimeType] = None,
    status: Option[BMEssenceLocatorStatusType] = None,
    file: Seq[java.net.URI] = Nil) extends ListFileLocatorType

trait FolderLocatorType extends BMEssenceLocatorType {
    val folder: Option[java.net.URI]
}
    
/** Parent of a folder structure that, taken as a whole including its children, represents an essence location.
 *  The folder and all of its children should be presented within a single filing system.
 */
case class FolderLocator(
    resourceParameters: ResourceParameters,
    storageType: Option[Storage] = None,
    locatorInfo: Option[String] = None,
    containerMimeType: Option[ContainerMimeType] = None,
    status: Option[BMEssenceLocatorStatusType] = None,
    folder: Option[java.net.URI] = None) extends FolderLocatorType
    
sealed trait StorageTypes

object StorageTypes {
  def fromString(value: String): StorageTypes = value match {
    case "online" => Online
    case "offline" => Offline
    case "hsm" => Hsm
    case "archive" => Archive
    case "playout" => Playout
    case "other" => Other
    case _ => throw new IllegalArgumentException("String value '" + value + "' does not match storage type enumeration.")
  }
  case object Online extends StorageTypes { override def toString = "online" }
  case object Offline extends StorageTypes { override def toString = "offline" } 
  case object Hsm extends StorageTypes { override def toString = "hsm" }
  case object Archive extends StorageTypes { override def toString = "archive" }
  case object Playout extends StorageTypes { override def toString = "playout" }
  case object Other extends StorageTypes { override def toString = "other" }
}

/** Different kinds of storage media available (e.g. online on disk), in combination with type 
 *  group attributes.
*/
case class Storage(
    value: StorageTypes,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    attributes: Option[Attribute] = None) extends TypeGroup[Storage] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
}

case class ContainerMimeType(
    value: String,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None) extends TypeGroup[ContainerMimeType] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
}

trait BMEssenceLocatorStatusType extends ResourceType {
  val resourceParameters: ResourceParameters
  val status: BMStatusType
  val subStatus: Option[BMCustomStatusExtensionType]
}

case class BMEssenceLocatorStatus(
    resourceParameters: ResourceParameters,
    status: BMStatusType,
    subStatus: Option[BMCustomStatusExtensionType]) extends BMEssenceLocatorStatusType