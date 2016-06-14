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

package tv.amwa.ebu.fims.rest.rest.resource
import tv.amwa.ebu.fims.rest.commons.Loggable
import tv.amwa.ebu.fims.rest.converter.FimsXML
import tv.amwa.ebu.fims.rest.engine.ServiceEngine
import tv.amwa.ebu.fims.rest.model.ResourceID.stringToResourceID
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.applyDetail
import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.contentToFull
import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.contentToLink
import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.contentToSummary
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.rest.handling.QueryParamsHandling.getParam
import tv.amwa.ebu.fims.rest.rest.handling.Readers.ContainerTypeReader
import tv.amwa.ebu.fims.rest.rest.handling.Readers.IntReader
import tv.amwa.ebu.fims.rest.rest.handling.RequestHandling
import tv.amwa.ebu.fims.rest.rest.model.Container
import tv.amwa.ebu.fims.rest.rest.model.ContainerType
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.rest.model.FullType
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import tv.amwa.ebu.fims.rest.rest.model.LinkType
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.rest.model.SummaryType
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("content")
class BMContentContainerResource(contentEngine: ServiceEngine[BMContentType]) extends RequestHandling with Loggable {
  import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.applyDetail
  
  @GET 
  @Produces(Array(MediaType.APPLICATION_XML))
  def listBMContents(@Context uriInfo: UriInfo): Response = { 
    val queryParams = uriInfo.getQueryParameters
    val res = for {
      skip <- getParam[Int]("skip", queryParams).right
      limit <- getParam[Int]("limit", queryParams).right
      detail <- getParam[ContainerType]("detail", queryParams).right
      content <- contentEngine.listResources(skip, limit).right
    } yield {
      val totalSize = contentEngine.countResources
      detail.getOrElse(FullType) match {
        case LinkType => FimsXML.write(new Container[BMContentType, LinkItem](totalSize, skip, limit, content))
        case SummaryType => FimsXML.write(new Container[BMContentType, SummaryItem](totalSize, skip, limit, content))
        case FullType => FimsXML.write(new Container[BMContentType, FullItem](totalSize, skip, limit, content))
      }
    }
    httpOK(res)
  }
  
  @POST
  @Consumes(Array(MediaType.APPLICATION_XML))
  @Produces(Array(MediaType.APPLICATION_XML))
  def createBMContent(contentDetails: BMContentType, @Context uriInfo: UriInfo): Response = { 
    httpCreated(applyDetail(contentEngine.createResource(contentDetails), uriInfo), uriInfo)
  }
  
  @POST
  @Path("purge")
  def purge = {
    contentEngine.purge
    httpNoContent   
  }
  
  @DELETE
  def delete(): Response = { 
    contentEngine.deleteAll
    httpNoContent
  }
  
  @Path("{resourceID}")
  def getJob(@PathParam("resourceID") resourceID: String): BMContentResource = new BMContentResource(contentEngine, resourceID)
}