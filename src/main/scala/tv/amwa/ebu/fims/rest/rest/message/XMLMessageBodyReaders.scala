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
import tv.amwa.ebu.fims.rest.converter.Reader
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import scala.xml.NodeSeq
import javax.ws.rs.ext.MessageBodyReader
import java.lang.reflect.Type
import java.lang.annotation.Annotation
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import java.io.InputStream
import tv.amwa.ebu.fims.rest.converter.FimsXML
import tv.amwa.ebu.fims.rest.Constants
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import javax.ws.rs.Consumes
import javax.ws.rs.ext.Provider
import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters.HTTPErrorConverter
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.ManageJobRequestType
import tv.amwa.ebu.fims.rest.model.ManageQueueRequestType
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe => ru}
import scala.reflect.ClassTag
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.commons.IOUtil
import java.io.InputStreamReader
import scala.xml.XML
import tv.amwa.ebu.fims.rest.model.transform.TransformJobType
import tv.amwa.ebu.fims.rest.model.transfer.TransferJobType
import tv.amwa.ebu.fims.rest.model.StartProcessType
import tv.amwa.ebu.fims.rest.model.StopProcessType

object JavaTypeFinder {
  implicit def getType[T](clazz: Class[T]): ru.Type = {
    val runtimeMirror =  ru.runtimeMirror(clazz.getClassLoader)
    runtimeMirror.classSymbol(clazz).toType
  }
}

abstract class GenericXMLMessageBodyReader[C : TypeTag](implicit val xmlReader: Reader[C,NodeSeq]) extends MessageBodyReader[C] {
  import JavaTypeFinder.getType
  def isReadable(aClass : Class[_], genericType : Type, annotations : Array[Annotation], mediaType : MediaType) = {
	// println("Class is " + aClass.toString + " and generic type is " + genericType.toString + " with class tag " + implicitly[TypeTag[C]].toString)
	aClass <:< typeOf[C] }
  def readFrom(aClass : Class[C], genericType : Type, annotations : Array[Annotation], mediaType : MediaType, 
      httpHeaders : MultivaluedMap[String, String], entityStream : InputStream) : C = {
	FimsXML.fromStream[C](entityStream, Constants.DEFAULT_XML_ENCODING)
  }  
}

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object JobXMLMessageBodyReader extends MessageBodyReader[JobType] {
  import JavaTypeFinder.getType
  def isReadable(aClass : Class[_], genericType : Type, annotations : Array[Annotation], mediaType : MediaType) = 
    aClass <:< classOf[JobType]
  def readFrom(aClass : Class[JobType], genericType : Type, annotations : Array[Annotation], mediaType : MediaType, 
      httpHeaders : MultivaluedMap[String, String], entityStream : InputStream) : JobType = {
    
	IOUtil.withReader(new InputStreamReader(entityStream, "UTF-8")) {isr =>
      val xml = XML.load(isr)
      (xml(0).attribute("http://www.w3.org/2001/XMLSchema-instance", "type")).get.text.split(':')(1) match {
        case "CaptureJobType" => FimsXML.read[CaptureJobType](xml)
        case "TransformJobType" => FimsXML.read[TransformJobType](xml)
        case "TransferJobType" => FimsXML.read[TransferJobType](xml)
      }
    }
  }  
}

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object StartProcessMessageBodyReader extends GenericXMLMessageBodyReader[StartProcessType]

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object StopProcessMessageBodyReader extends GenericXMLMessageBodyReader[StopProcessType]

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object BMContentXMLMessageBodyReader extends GenericXMLMessageBodyReader[BMContentType]

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object ManageJobXMLMessageBodyReader extends GenericXMLMessageBodyReader[ManageJobRequestType]

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object ManageQueueXMLMessageBodyReader extends GenericXMLMessageBodyReader[ManageQueueRequestType]

@Provider
@Consumes(Array(MediaType.APPLICATION_XML))
object HTTPErrorXMLMessageBodyReader extends GenericXMLMessageBodyReader[HTTPError] {
  override def readFrom(aClass : Class[HTTPError], genericType : Type, annotations : Array[Annotation], mediaType : MediaType, 
      httpHeaders : MultivaluedMap[String, String], entityStream : InputStream) : HTTPError = {
	val error = FimsXML.fromStream[HTTPError](entityStream, Constants.DEFAULT_XML_ENCODING)
	if (error.fault.description.getOrElse("").matches("""[0-9]{3}.*""")) error.copy(statusCode = error.fault.description.get.take(3).toInt)
	else error
  }  
}