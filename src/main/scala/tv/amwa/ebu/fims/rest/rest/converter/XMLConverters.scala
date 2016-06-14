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
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError
import scala.xml.NodeSeq
import scala.xml.NodeSeq.Empty
import tv.amwa.ebu.fims.rest.rest.model.Link
import scala.xml.Text
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.rest.model.Item
import tv.amwa.ebu.fims.rest.converter.XMLConverters.FaultConverter
import tv.amwa.ebu.fims.rest.converter.Converter
import tv.amwa.ebu.fims.rest.converter.XMLNamespaceProcessor
import scala.xml.NamespaceBinding
import scala.xml.TopScope
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobParameters
import tv.amwa.ebu.fims.rest.model.JobStatusType
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.BMContent
import tv.amwa.ebu.fims.rest.model.BMContent
import tv.amwa.ebu.fims.rest.model.BMContentFormat
import tv.amwa.ebu.fims.rest.model.Description
import tv.amwa.ebu.fims.rest.model.BMContentDescription
import tv.amwa.ebu.fims.rest.model.BMObject
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfile
import tv.amwa.ebu.fims.rest.model.BaseProfileParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfileParameters
import tv.amwa.ebu.fims.rest.model.Service
import tv.amwa.ebu.fims.rest.model.AudioFormat

object XMLConverters {

  val atomNS = NamespaceBinding("atom", "http://www.w3.org/2005/Atom", TopScope)
  
  implicit object HTTPErrorConverter extends Converter[HTTPError, NodeSeq] {
    def write(details: HTTPError) = {
      FaultConverter.write(details.fault)
    }
    def read(nodes: NodeSeq) = {
      HTTPError(0, FaultConverter.read(nodes))
    }
  }

//  private[this] def writeSelfLink(what: Link) = {
//    XMLNamespaceProcessor.setNameSpaceIfAbsent(<atom:link href={Text(what.href)} rel={what.rel.map(Text(_))}/>, atomNS)
//  }
//
//  private[this] def writeNextJobStates(status: JobStatusType, what: Link) = {
//    XMLNamespaceProcessor.setNameSpaceIfAbsent(
//        <atom:link href={Text(what.href)} rel={what.rel.map{Text(_)}}/> ++
//        (status match {
//          case JobStatusType.Queued => List("cancel")
//          case JobStatusType.Running => List("cancel", "stop", "pause", "restart")
//          case JobStatusType.Paused => List("resume")
//          case JobStatusType.Completed | JobStatusType.Canceled | JobStatusType.Stopped | JobStatusType.Failed => List("cleanup")
//          case JobStatusType.Unknown => Nil
//        }).map{x => <atom:link href={Text(what.href.stripSuffix("/") + "/" + x)} rel={Text(x)}/>}, 
//        atomNS)
//  }
  
  implicit object JobLinkItemWriter extends Writer[LinkItem[JobType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.CaptureJobConverter
    def write(what: LinkItem[JobType]) = { 
      what.value match {
        case CaptureJob(rp, bp, sp) => implicitly[Writer[CaptureJobType, NodeSeq]].write(
            CaptureJob(rp.copy(resourceID = rp.resourceID, revisionID = rp.revisionID, location = None, resourceCreationDate = None, resourceModifiedDate = None /*,
                extensionGroup = Some(writeSelfLink(what.link))*/), BaseJobParameters(), CaptureJobParameters()))
        case _ => Empty
      } 
    }
  }

  implicit object JobSummaryItemWriter extends Writer[SummaryItem[JobType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.CaptureJobConverter
    def write(what: SummaryItem[JobType]) = { 
      what.value match {
        case CaptureJob(rp, bp, sp) => implicitly[Writer[CaptureJobType, NodeSeq]].write(
            CaptureJob(rp /*.copy(extensionGroup = Some(writeNextJobStates(bp.status.getOrElse(JobStatusType.Unknown), what.link))) */, 
                bp.copy(
                    bmObjects = bp.bmObjects.map{
                      case BMObject(orp, contents) => BMObject(ResourceParameters(resourceID = orp.resourceID, revisionID = orp.revisionID), 
                          contents.map{ 
                            case BMContent(crp, formats, descriptions, status) => BMContent(ResourceParameters(resourceID = crp.resourceID, revisionID = crp.revisionID), Nil, Nil)
                            case c => c
                          })
                      case o => o 
                    }), sp.copy(
                    profiles = sp.profiles.map{
                      case CaptureProfile(rp, bp, pp) => CaptureProfile(ResourceParameters(resourceID = rp.resourceID, revisionID = rp.revisionID), 
                          BaseProfileParameters(), 
                          CaptureProfileParameters())
                      case p => p
                     })))
        case _ => Empty
      } 
    }
  }
  
  implicit object JobFullItemWriter extends Writer[FullItem[JobType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.CaptureJobConverter
    def write(what: FullItem[JobType]) = { 
      what.value match {
        case CaptureJob(rp, bp, sp) => implicitly[Writer[CaptureJobType, NodeSeq]].write(
            CaptureJob(rp.copy(resourceID = rp.resourceID, revisionID = rp.revisionID /*,
                extensionGroup = Some(writeNextJobStates(bp.status.getOrElse(JobStatusType.Unknown), what.link) ) */), 
                bp, sp))
        case _ => Empty
      } 
    }
  }

  implicit object BMContentLinkItemWriter extends Writer[LinkItem[BMContentType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.BMContentConverter
    def write(what: LinkItem[BMContentType]) = { 
      what.value match {
        case BMContent(rp, formats, descriptions, status) => implicitly[Writer[BMContentType, NodeSeq]].write(
            BMContent(ResourceParameters(resourceID = rp.resourceID, revisionID = rp.revisionID /*, 
                extensionGroup = Some(writeSelfLink(what.link)) */), Nil, Nil))
        case _ => Empty
      } 
    }
  }
  
  implicit object BMContentSummaryItemWriter extends Writer[SummaryItem[BMContentType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.BMContentConverter
    def write(what: SummaryItem[BMContentType]) = { 
      what.value match {
        case BMContent(rp, formats, descriptions, status) => implicitly[Writer[BMContentType, NodeSeq]].write(
            BMContent(rp /*.copy(extensionGroup = Some(rp.extensionGroup.getOrElse(Empty) ++ writeSelfLink(what.link))) */, 
                formats.map{_ match { case f : BMContentFormat => BMContentFormat(f.resourceParameters, Nil, None, None, None, None, None) ; case f => f}}, 
                descriptions.map{_ match { 
                  case d : Description => Description(d.resourceParameters, d.bmContentDescription.map{x => BMContentDescription(title = x.title)})
                  case d => d
                }}, status))
        case _ => Empty
      } 
    }
  }

  implicit object BMContentFullItemWriter extends Writer[FullItem[BMContentType], NodeSeq] {
    import tv.amwa.ebu.fims.rest.converter.XMLConverters.BMContentConverter
    def write(what: FullItem[BMContentType]) = { 
      what.value match {
        case bmc @ BMContent(rp, formats, descriptions, status) => implicitly[Writer[BMContentType, NodeSeq]].write(
            /* BMContent(rp.copy(extensionGroup = Some(rp.extensionGroup.getOrElse(Empty) ++ writeSelfLink(what.link))), 
                formats, descriptions, status)*/ bmc)
        case _ => Empty
      } 
    }    
  }
  
//  implicit def itemWriter[Item[T], NodeSeq](implicit xmlWriter: Writer[M[T], NodeSeq]) = xmlWriter 

  implicit def containerWriter[T, M[_] <: Item[_]](implicit mt: Manifest[T], mt2: Manifest[M[_]], ev: Writer[M[T], NodeSeq]) = new ContainerXMLWriter[T, M]
}