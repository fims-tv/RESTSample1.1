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
import tv.amwa.ebu.fims.rest.engine.ServiceEngine
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.ResourceID
import tv.amwa.ebu.fims.rest.rest.handling.RequestHandling
import javax.ws.rs.core.Response
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo

class BMContentResource(val contentEngine: ServiceEngine[BMContentType], val resourceID: ResourceID) extends RequestHandling with Loggable {
  import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.applyDetail
  
  @GET
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def getJob(@Context uriInfo: UriInfo): Response = httpOK(applyDetail(contentEngine.getResource(resourceID), uriInfo))

  @POST
  @Consumes(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def updateJob(contentDetails: BMContentType, @Context uriInfo: UriInfo): Response = 
    httpOK(applyDetail(contentEngine.updateResource(resourceID, contentDetails), uriInfo))
    
  @DELETE
  def deleteJob: Response = httpNoContent(contentEngine.delete(resourceID))
}
