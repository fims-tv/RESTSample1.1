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
import javax.ws.rs.core.Response.Status
import javax.ws.rs.WebApplicationException
import tv.amwa.ebu.fims.rest.engine.NotFoundException
import tv.amwa.ebu.fims.rest.engine.ItemAlreadyExistsException
import tv.amwa.ebu.fims.rest.engine.ForbiddenException
import tv.amwa.ebu.fims.rest.model.Fault
import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import org.xml.sax.SAXParseException
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

case class HTTPError(statusCode: Int, fault: FaultType)

@Provider
object WebApplicationExceptionMapper extends ExceptionMapper[Throwable] {
  private[this] implicit def toStatusInfo(status: Status) = (status.getStatusCode, status.getReasonPhrase) 
  private[this] def getStatus(ex : Throwable) : Pair[Int,String] = ex match{
    case _ : NotFoundException 				=> Status.NOT_FOUND
    case _ : IllegalArgumentException	 	=> Status.BAD_REQUEST
    case _ : ItemAlreadyExistsException 	=> Status.CONFLICT
    case _ : ForbiddenException				=> Status.FORBIDDEN
    case _ : SAXParseException				=> Status.BAD_REQUEST
    case wae : WebApplicationException if wae.getCause != null		=> getStatus(wae.getCause)
    case _ : Exception						=> Status.INTERNAL_SERVER_ERROR
  }  
  private[this] def getFIMSCode(ex: Throwable): ErrorCodeType = 
    if (ex.getMessage != null && ex.getMessage.matches("""(INF|DAT|SEC|SVC|EXT)_S00_[0-9]{4}.*""")) ErrorCodeType.fromString(ex.getMessage.take(12)) 
    else (ex match {
      case _ : NotFoundException => ErrorCodeType.DAT_S00_0003 // TODO rationalize these error codes
      case _ : IllegalArgumentException	=> ErrorCodeType.DAT_S00_0001
      case _ : ItemAlreadyExistsException => ErrorCodeType.DAT_S00_0005
      case _ : ForbiddenException => ErrorCodeType.DAT_S00_0007 
      case _ : SAXParseException => ErrorCodeType.DAT_S00_0001
      case wae : WebApplicationException if wae.getCause != null => getFIMSCode(wae.getCause)
      case _ : Exception => ErrorCodeType.INF_S00_0003   
    })
  override def toResponse(error: Throwable)  = {
    val status = getStatus(error) 
    val fimsCode = getFIMSCode(error)
    Response
    .status(status._1)
    .header("X-FIMS-Version", "1.1")
    .entity(HTTPError(status._1, Fault(fimsCode,Some(status._1 +" " + status._2),Some(error.getMessage))))
    .build()
  }  
  def apply(error :Throwable) = throw new WebApplicationException(toResponse(error))
}