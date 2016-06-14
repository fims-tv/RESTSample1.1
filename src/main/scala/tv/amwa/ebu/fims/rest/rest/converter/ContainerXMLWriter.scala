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

package tv.amwa.ebu.fims.rest.rest.converter
import tv.amwa.ebu.fims.rest.rest.model.Item
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.rest.model.Container
import scala.xml.NodeSeq
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import scala.xml.Text
import tv.amwa.ebu.fims.rest.converter.XMLNamespaceProcessor
import tv.amwa.ebu.fims.rest.Namespaces

class ContainerXMLWriter[T, M[_] <: Item[_]](implicit mt: Manifest[T], mt2: Manifest[M[_]], ev: Writer[M[T], NodeSeq]) extends Writer[Container[T, M], NodeSeq] {
  val FullItemType = classOf[FullItem[_]]
  val SummaryItemType = classOf[SummaryItem[_]]
  val LinkItemType = classOf[LinkItem[_]]
  def write(what: Container[T, M]) = {
    val List(pageAttribute, pagesAttribute) = List(what.page, what.pages).map(option => option.map(i => new Text(i.toString)))
    val detail = mt2.runtimeClass match {
      case FullItemType => "full"
      case SummaryItemType => "summary"
      case LinkItemType => "link"
    }
    val (prefix, label) = {
       mt.runtimeClass.getSimpleName match {
        case "JobType" => ("bms", "jobs")
        case "BMContentType" => ("bms", "bmContents")
        case "PropertyInfoType" => ("rps", "propertyInfos")
        case _ => ("bms", "resources")
      }
    }
    XMLNamespaceProcessor.setNameSpaceIfAbsent(
        <xml detail={detail} totalSize={new Text(what.totalSize.toString)} page={pageAttribute} pages={pagesAttribute}>{
        	what.items.map{v => ev.write(v) } }</xml>.copy(prefix = prefix, label = label),
        Namespaces.bms)
  }
}
