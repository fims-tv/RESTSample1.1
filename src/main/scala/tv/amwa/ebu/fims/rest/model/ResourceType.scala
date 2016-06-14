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
import tv.amwa.ebu.fims.rest.converter.Converter

/** Each Type includes an ExtensionGroup
 *  to allow for vendor-specific extensions, but the definition is out of scope of the 
 *  FIMS specification. 
 */
// case class ExtensionGroup(any: scalaxb.DataRecord[Any]*)


/** Each Type includes an ExtensionAttributes
 *  to allow for vendor-specific extensions, but the definition is out of scope of the 
 *  FIMS specification.
 */
// case class ExtensionAttributes(attributes: Map[String, scalaxb.DataRecord[Any]])

/** The source or target of a reference to a
 *  distinct resource. Resources include queues, jobs, assets, descriptions, formats and
 *  profiles. 
 */
trait ResourceReferenceType {
  val resourceID: ResourceID
  val revisionID: Option[String]
  val location: Option[java.net.URI]
}

/** The source or target of a reference to a 
 *  distinct resource. Resources include queues, jobs, assets, descriptions, formats and
 *  profiles. 
*/
case class ResourceReference(
  resourceID: ResourceID,
  revisionID: Option[String] = None,
  location: Option[java.net.URI] = None) extends ResourceReferenceType
  
/** Resources include queues, jobs, assets,
 *  descriptions, formats and profiles. 
*/
trait ResourceType extends ResourceReferenceType {
  val resourceParameters: ResourceParameters
  lazy val resourceID = resourceParameters.resourceID
  lazy val revisionID = resourceParameters.revisionID
  lazy val location  = resourceParameters.location
  lazy val resourceCreationDate = resourceParameters.resourceCreationDate
  lazy val resourceModifiedDate = resourceParameters.resourceModifiedDate
  lazy val notifyAt = resourceParameters.notifyAt
  lazy val serviceGeneratedElement = resourceParameters.serviceGeneratedElement
  lazy val isFullyPopulated = resourceParameters.isFullyPopulated
  lazy val extensionGroup = resourceParameters.extensionGroup
  lazy val extensionAttributes = resourceParameters.extensionAttributes
}

case class ResourceParameters(
  resourceID: ResourceID,
  revisionID: Option[String] = None,
  location: Option[java.net.URI] = None,
  resourceCreationDate: Option[Long] = None,
  resourceModifiedDate: Option[Long] = None,
  notifyAt: Option[AsyncEndpoint] = None,
  serviceGeneratedElement: Option[Boolean] = None,
  isFullyPopulated: Option[Boolean] = None,
  extensionGroup: Option[scala.xml.NodeSeq] = None,
  extensionAttributes: Option[scala.xml.MetaData] = None) {
  def next = {
    val currentDate = System.currentTimeMillis
    copy(
      revisionID = revisionID match { 
        case None => Some("1"); 
        case Some(number) if number.matches("[0-9]+") => Some((number.toLong + 1).toString); 
        case x @ _ => x},
      resourceCreationDate = resourceCreationDate match {
        case None => Some(currentDate)
        case Some(creationDate) => Some(creationDate)
      },
      resourceModifiedDate = Some(currentDate) )
  } 
}


/** Provides endpoints where a service can
				send back a notification for a job completed or failed. 
*/
case class AsyncEndpoint(
  replyTo: java.net.URI,
  faultTo: java.net.URI)
  
/**
 *   If updating a vendor specific extension (ExtensionGroup) for a given ResourceReferenceType,
 *   the usage of CustomValueType should be used.
 */  
trait CustomValueType
