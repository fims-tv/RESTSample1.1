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
import scala.xml.NodeSeq

trait ServiceType extends ResourceType {
  val providerName: Option[String]
  val providerEndPoint: Option[java.net.URI]
  val serviceDescription: Option[NodeSeq]
}

case class Service(
    resourceParameters: ResourceParameters,
    providerName: Option[String] = None,
    providerEndPoint: Option[java.net.URI] = None,
    serviceDescription: Option[NodeSeq] = None) extends ServiceType