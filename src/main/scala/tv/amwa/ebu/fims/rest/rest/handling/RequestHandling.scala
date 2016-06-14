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

package tv.amwa.ebu.fims.rest.rest.handling
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import tv.amwa.ebu.fims.rest.model.ResourceType
import tv.amwa.ebu.fims.rest.converter.FimsString
import tv.amwa.ebu.fims.rest.converter.StringConverters._
import tv.amwa.ebu.fims.rest.rest.model.Item

trait RequestHandling {
  def httpOK[T](res: Either[Throwable, T]): Response = {
    res.fold(e => WebApplicationExceptionMapper.toResponse(e), t => Response.ok().header("X-FIMS-Version", "1.1").entity(t).build())
  }
  def httpCreated[T](res: Either[Throwable, Item[ResourceType]], uriInfo: UriInfo): Response = {
    res.fold(e => WebApplicationExceptionMapper.toResponse(e), 
      item => { 
        val locationURI = uriInfo.getRequestUriBuilder()
          .replaceQuery(null).fragment(null)
          .path(FimsString.write(item.value.resourceID)).build()
        Response.created(locationURI).header("X-FIMS-Version", "1.1").entity(item).build()
      })
  }
  def httpNoContent[T](res: Either[Throwable, T]): Response = {
    res.fold(e => WebApplicationExceptionMapper.toResponse(e), _ => Response.noContent().header("X-FIMS-Version", "1.1").build())
  }
  def httpAsync[T](res: Either[Throwable, T]): Response = {
    res.fold(e => WebApplicationExceptionMapper.toResponse(e), _ => Response.status(202).header("X-FIMS-Version", "1.1").build())
  }
  def httpNoContent: Response = httpNoContent(Right())
}