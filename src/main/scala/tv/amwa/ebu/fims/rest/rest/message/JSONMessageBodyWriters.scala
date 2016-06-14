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

package tv.amwa.ebu.fims.rest.rest.message
import java.io.OutputStream
import java.lang.reflect.Type
import java.lang.annotation.Annotation
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.model.JobType
import scala.xml.NodeSeq
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import tv.amwa.ebu.fims.rest.converter.FimsJSON
import tv.amwa.ebu.fims.rest.Constants
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import javax.ws.rs.ext.Provider
import javax.ws.rs.Produces
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError

abstract class GenericJSONMessageBodyWriter[T](implicit val manifest: Manifest[T], implicit val xmlWriter: Writer[T,NodeSeq]) extends MessageBodyWriter[T] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = manifest.runtimeClass.isAssignableFrom(aClass)
  def getSize(t: T, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo( t: T, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
	FimsJSON.toStream(t, outputStream, Constants.DEFAULT_XML_ENCODING)
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object JobJSONMessageBodyWriter extends GenericJSONMessageBodyWriter[CaptureJobType] 

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object BMContentJSONMessageBodyWriter extends GenericJSONMessageBodyWriter[BMContentType]

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object FullItemJSONMessageBodyWriter extends MessageBodyWriter[FullItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[FullItem[_]].isAssignableFrom(aClass)
  def getSize(t: FullItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: FullItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsJSON.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsJSON.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object SummaryItemJSONMessageBodyWriter extends MessageBodyWriter[SummaryItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[SummaryItem[_]].isAssignableFrom(aClass)
  def getSize(t: SummaryItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: SummaryItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsJSON.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsJSON.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object LinkItemJSONMessageBodyWriter extends MessageBodyWriter[LinkItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[LinkItem[_]].isAssignableFrom(aClass)
  def getSize(t: LinkItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: LinkItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsJSON.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsJSON.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_JSON))
object HTTPErrorJSONMessageBodyWriter extends GenericJSONMessageBodyWriter[HTTPError]
