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

package tv.amwa.ebu.fims.rest
import scala.xml.NamespaceBinding
import scala.xml.TopScope

object Namespaces {
  val xsi = NamespaceBinding("xsi", "http://www.w3.org/2001/XMLSchema-instance", TopScope)
  val xml = NamespaceBinding("xml", "http://www.w3.org/XML/1998/namespace", xsi)
  val tim = NamespaceBinding("tim", "http://baseTime.fims.tv", xml)
  val desc = NamespaceBinding("desc", "http://description.fims.tv", tim)
  val bms = NamespaceBinding("bms", "http://base.fims.tv", desc)
  val cmsDefault = NamespaceBinding(null, "http://capturemedia.fims.tv", bms)
  val cms = NamespaceBinding("cms", "http://capturemedia.fims.tv", bms)
  val tfms = NamespaceBinding("tfms", "http://transformmedia.fims.tv", bms)
  val tms = NamespaceBinding("tms", "http://transfermedia.fims.tv", bms)
  val rps = NamespaceBinding("rps", "http://repository.fims.tv", bms)
}
