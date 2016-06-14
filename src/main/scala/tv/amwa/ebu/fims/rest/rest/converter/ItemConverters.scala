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
import tv.amwa.ebu.fims.rest.model.ResourceType
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import tv.amwa.ebu.fims.rest.rest.model.Link
import tv.amwa.ebu.fims.rest.converter.FimsString
import tv.amwa.ebu.fims.rest.converter.StringConverters.ResourceIDConverter
import tv.amwa.ebu.fims.rest.rest.handling.QueryParamsHandling._
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobParameters
import scala.xml.Text
import tv.amwa.ebu.fims.rest.model.BMContentType
import javax.ws.rs.core.UriInfo
import tv.amwa.ebu.fims.rest.rest.model.Item
import tv.amwa.ebu.fims.rest.rest.model.ContainerType
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.rest.handling.Readers._
import tv.amwa.ebu.fims.rest.rest.model.FullType
import tv.amwa.ebu.fims.rest.rest.model.LinkType
import tv.amwa.ebu.fims.rest.rest.model.SummaryType

object ItemConverters {
  implicit object jobToLink extends Writer[JobType, LinkItem[JobType]] {
    def write(from: JobType) = LinkItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  implicit object jobToSummary extends Writer[JobType, SummaryItem[JobType]] {
    def write(from: JobType) = SummaryItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  implicit object jobToFull extends Writer[JobType, FullItem[JobType]] {
    def write(from: JobType) = FullItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  implicit object contentToLink extends Writer[BMContentType, LinkItem[BMContentType]] {
    def write(from: BMContentType) = LinkItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  implicit object contentToSummary extends Writer[BMContentType, SummaryItem[BMContentType]] {
    def write(from: BMContentType) = SummaryItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  implicit object contentToFull extends Writer[BMContentType, FullItem[BMContentType]] {
    def write(from: BMContentType) = FullItem(from, Link(from.location.map(_.toString).getOrElse(FimsString.write(from.resourceID)), Some("self")))
  }
  
  def applyDetail[T <: ResourceType](fullResource: Either[Throwable,T], uriInfo: UriInfo): Either[Throwable,Item[T]] = {
    val queryParameters = uriInfo.getQueryParameters
    for {
        detail <- getParam[ContainerType]("detail", queryParameters).right
        job <- fullResource.right
    } yield {
      detail.getOrElse(FullType) match {
        case LinkType => LinkItem(job, Link(job.location.map{_.toString}.getOrElse(FimsString.write(job.resourceID)), Some("self")))
        case SummaryType => SummaryItem(job, Link(job.location.map{_.toString}.getOrElse(FimsString.write(job.resourceID)), Some("self")))
        case FullType => FullItem(job, Link(job.location.map{_.toString}.getOrElse(FimsString.write(job.resourceID)), Some("self")))
      }
    }
  }
}