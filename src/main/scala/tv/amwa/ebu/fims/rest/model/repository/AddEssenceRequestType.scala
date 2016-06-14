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

package tv.amwa.ebu.fims.rest.model.repository

import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.BMContentFormatType
import tv.amwa.ebu.fims.rest.model.BMEssenceLocatorType
import tv.amwa.ebu.fims.rest.model.AsyncEndpoint
import tv.amwa.ebu.fims.rest.model.BMEssenceLocatorType
import tv.amwa.ebu.fims.rest.model.EssencePlaceholderLocatorType
import tv.amwa.ebu.fims.rest.model.ResourceReferenceType

trait AddEssenceRequestType { // Credentials assumed in header for REST ... will need to add for SOAP support
  val content: BMContentType
  val format: Option[BMContentFormatType]
  val essence: BMEssenceLocatorType
  val essencePlaceholder: Option[EssencePlaceholderLocatorType] // TODO marked as mandatory in request, yet is described as optional
  val notifyAt: Option[AsyncEndpoint]
}

case class AddEssenceRequest( 
    content: BMContentType,
    format: Option[BMContentFormatType],
    essence: BMEssenceLocatorType,
    essencePlaceholder: Option[EssencePlaceholderLocatorType] = None,
    notifyAt: Option[AsyncEndpoint] = None) extends AddEssenceRequestType
    
trait AddEssenceOperationAckType {
  val timeStamp: Long
  val operationID: ResourceReferenceType
}

case class AddEssenceOperationAck (
    timeStamp: Long,
    operationID: ResourceReferenceType) extends AddEssenceOperationAckType