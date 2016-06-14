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

trait ServiceProfileParameters

/** A profile used by a FIMS media service to
 *  perform a job on media content. For example, it may represent the profile of a
 *  transfer media service for transferring media content and, as such, may specify the
 *  media format to be produced in output. The profile provides a mechanism to specify
 *  service-provider-specific information for each operation. 
*/
trait ProfileType extends ResourceType {
    val resourceParameters: ResourceParameters
    val baseParameters: BaseProfileParameters
    val serviceParameters: ServiceProfileParameters
    lazy val service = baseParameters.service
    lazy val name = baseParameters.name
    lazy val description = baseParameters.description
}

case class BaseProfileParameters(
    service: Option[ServiceType] = None,
    name: Option[String] = None,
    description: Option[String] = None)
  
/** Parameters specific to transfer media services that can be re-used for other services. 
 *  Note: Some parameters might be added to the atom in future version to specify network 
 *  resource utilization, a list of acceptable transfer mechanisms or additional security 
 *  options including whether the received files should be checked against a fingerprint 
 *  and whether they are encrypted. 
*/
case class TransferAtom(
    destination: java.net.URI
//  ExtensionGroup: Option[com.quantel.qstack.industry.model.ExtensionGroup] = None,
//  ExtensionAttributes: Option[com.quantel.qstack.industry.model.ExtensionAttributes] = None
    )


/** Parameters specific to the transform
 *  media service that can be re-used by other services. Note: Some parameters might be
 *  added to this type in future version of this specification to specify AV Process, etc.
*/
case class TransformAtom(
    videoFormat: Option[VideoFormatType] = None,
    audioFormat: Option[AudioFormatType] = None,
    containerFormat: Option[ContainerFormatType] = None
//    ExtensionGroup: Option[com.quantel.qstack.industry.model.ExtensionGroup] = None,
//  ExtensionAttributes: Option[com.quantel.qstack.industry.model.ExtensionAttributes] = None)
    )
