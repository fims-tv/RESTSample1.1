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

import tv.amwa.ebu.fims.rest.model.ResourceType
import tv.amwa.ebu.fims.rest.model.ResourceReferenceType
import tv.amwa.ebu.fims.rest.model.ResourceParameters

trait LockTokenType extends ResourceType {
  val expiration: Option[Long]
  val resourceReferences: Seq[ResourceReferenceType]
}

case class LockToken(
    resourceParameters: ResourceParameters,
    expiration: Option[Long] = None,
    resourceReferences: Seq[ResourceReferenceType] = Nil) extends LockTokenType