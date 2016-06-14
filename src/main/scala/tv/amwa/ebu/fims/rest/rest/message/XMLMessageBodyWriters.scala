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
import scala.xml.NodeSeq
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.converter.FimsXML
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.Constants
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider
import javax.ws.rs.Produces
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters.HTTPErrorConverter
import scala.xml.Node
import tv.amwa.ebu.fims.rest.rest.model.FullItem
import tv.amwa.ebu.fims.rest.model.ResourceType
import tv.amwa.ebu.fims.rest.rest.model.SummaryItem
import tv.amwa.ebu.fims.rest.rest.model.LinkItem
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.rest.model.Item

abstract class GenericXMLMessageBodyWriter[T](implicit val manifestT: Manifest[T], implicit val xmlWriter: Writer[T, NodeSeq]) extends MessageBodyWriter[T] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    val writeable = manifestT.runtimeClass.isAssignableFrom(aClass)
//    println("Checking manifest " + manifestT.toString + " is assignable from " + aType.toString + " with result " + writeable)
    writeable
  }
  def getSize(t: T, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: T, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
	FimsXML.toStream(t, outputStream, Constants.DEFAULT_XML_ENCODING)
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object JobXMLMessageBodyWriter extends GenericXMLMessageBodyWriter[CaptureJobType]

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object BMContentXMLMessageBodyWriter extends GenericXMLMessageBodyWriter[BMContentType]

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object FullItemXMLMessageBodyWriter extends MessageBodyWriter[FullItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[FullItem[_]].isAssignableFrom(aClass)
  def getSize(t: FullItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: FullItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsXML.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsXML.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object SummaryItemXMLMessageBodyWriter extends MessageBodyWriter[SummaryItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[SummaryItem[_]].isAssignableFrom(aClass)
  def getSize(t: SummaryItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: SummaryItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsXML.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsXML.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object LinkItemXMLMessageBodyWriter extends MessageBodyWriter[LinkItem[_]] {
  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[LinkItem[_]].isAssignableFrom(aClass)
  def getSize(t: LinkItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo(t: LinkItem[_], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, 
      stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) = {
    t.value match {
      case cj : JobType => FimsXML.toStream(t.copy(value = cj), outputStream, Constants.DEFAULT_XML_ENCODING)
      case bmc : BMContentType => FimsXML.toStream(t.copy(value = bmc), outputStream, Constants.DEFAULT_XML_ENCODING)
    }
  }
}

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
object HTTPErrorXMLMessageBodyWriter extends GenericXMLMessageBodyWriter[HTTPError]

@Provider
@Produces(Array(MediaType.APPLICATION_XML))
class NodeXMLMessageBodyWriter extends MessageBodyWriter[Node]{
  def isWriteable( aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = classOf[Node].isAssignableFrom(aClass)
  def getSize(t: Node, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L
  def writeTo( t: Node, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream){
	  FimsXML.toStream(t, outputStream, Constants.DEFAULT_XML_ENCODING)
  }
}