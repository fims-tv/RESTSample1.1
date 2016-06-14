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

package tv.amwa.ebu.fims.rest.converter
import java.util.UUID
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers
import tv.amwa.ebu.fims.rest.model.VideoFormat
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.AudioFormat
import tv.amwa.ebu.fims.rest.model.AudioFormatType
import tv.amwa.ebu.fims.rest.model.ContainerFormat
import tv.amwa.ebu.fims.rest.model.ContainerFormatType
import tv.amwa.ebu.fims.rest.model.TechnicalAttribute
import tv.amwa.ebu.fims.rest.model.ContainerFormatParameters
import tv.amwa.ebu.fims.rest.model.Length
import tv.amwa.ebu.fims.rest.model.VideoFormatType
import tv.amwa.ebu.fims.rest.model.DataFormat
import tv.amwa.ebu.fims.rest.model.DataFormatType
import tv.amwa.ebu.fims.rest.model.Rational
import tv.amwa.ebu.fims.rest.model.Interlaced
import tv.amwa.ebu.fims.rest.model.Codec
import tv.amwa.ebu.fims.rest.model.BMTrack
import tv.amwa.ebu.fims.rest.model.Constant
import tv.amwa.ebu.fims.rest.model.Top
import tv.amwa.ebu.fims.rest.model.TrackConfiguration
import tv.amwa.ebu.fims.rest.model.Variable
import tv.amwa.ebu.fims.rest.model.IntegerType
import tv.amwa.ebu.fims.rest.model.CaptioningFormat
import tv.amwa.ebu.fims.rest.model.AncillaryDataFormat

@RunWith(classOf[JUnitRunner])
class FormatXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created video format" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val videoFormat = FormatXMLConversionSpec.videoFormat
        
      val outNodes = FimsXML.write(videoFormat)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[VideoFormatType](outNodes)
      println(videoFormat.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[VideoFormatType](videoFormat) should equal (videoFormat)
      XMLRoundTrip.writeToBytesThenRead[VideoFormatType](videoFormat) should equal (videoFormat)
    }
    
    "be validated against the FIMS schema" in {
      val videoFormat = FormatXMLConversionSpec.videoFormat
      
      val outNodes = FimsXML.write(videoFormat)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
  
  "A manually created audio format" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val audioFormat = FormatXMLConversionSpec.audioFormat
          
      val outNodes = FimsXML.write(audioFormat)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[AudioFormatType](outNodes)
      println(audioFormat.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[AudioFormatType](audioFormat) should equal (audioFormat)
      XMLRoundTrip.writeToBytesThenRead[AudioFormatType](audioFormat) should equal (audioFormat)
    }
    
    "be validated against the FIMS schema" in {
      val audioFormat = FormatXMLConversionSpec.videoFormat
      
      val outNodes = FimsXML.write(audioFormat)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }

  "A manually created data format" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val dataFormat = FormatXMLConversionSpec.dataFormat
          
      val outNodes = FimsXML.write(dataFormat)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[DataFormatType](outNodes)
      println(dataFormat.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[DataFormatType](dataFormat) should equal (dataFormat)
      XMLRoundTrip.writeToBytesThenRead[DataFormatType](dataFormat) should equal (dataFormat)
    }

    "be validated against the FIMS schema" in {
      val dataFormat = FormatXMLConversionSpec.dataFormat
      
      val outNodes = FimsXML.write(dataFormat)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }

  "A manually created container format" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val containerFormat = FormatXMLConversionSpec.containerFormat
          
      val outNodes = FimsXML.write(containerFormat)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[ContainerFormatType](outNodes)
      println(containerFormat.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[ContainerFormatType](containerFormat) should equal (containerFormat)
      XMLRoundTrip.writeToBytesThenRead[ContainerFormatType](containerFormat) should equal (containerFormat)
    }
    
    "be validated against the FIMS schema" in {
      val containerFormat = FormatXMLConversionSpec.containerFormat
      
      val outNodes = FimsXML.write(containerFormat)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object FormatXMLConversionSpec {
  val videoFormat = VideoFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      TechnicalAttribute("hazy", "tLabel", "tDefinition", "http://fims.tv/tLink", "fLabel", "fDefinition", "http://fims.tv/fLink"),
      Length(1920, "pixels"), Length(1024, "pixels"),
      Rational(30, 1000, 1001), Rational(1, 16, 9),
      Codec("codec name", "codec vendor", "codec version", "codec family", "tlabel", "tdefinition", "http://fims.tv/tlink"),
      BMTrack(UUIDBasedResourceID(UUID.randomUUID), "tlabel", "tdefinition", "http://fims.tv/tlink", "main video", "en-gb"),
      100000000l, Constant, 1080, Interlaced, Top, false) 

  val audioFormat = AudioFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      TechnicalAttribute("noisy", "tLabel", "tDefinition", "http://fims.tv/tLink", "fLabel", "fDefinition", "http://fims.tv/fLink"),
      48000.0,
      Codec("codec name", "codec vendor", "codec version", "codec family", "tlabel", "tdefinition", "http://fims.tv/tlink"),
      TrackConfiguration("tlabel", "tdefinition", "http://fims.tv/tlink"),
      List(BMTrack(UUIDBasedResourceID(UUID.randomUUID), "tlabel", "tdefinition", "http://fims.tv/tlink", "left audio", "en-gb"),
          BMTrack(UUIDBasedResourceID(UUID.randomUUID), "tlabel", "tdefinition", "http://fims.tv/tlink", "right audio", "en-gb")),
      2, 192000, Variable, 16, IntegerType)

  val dataFormat = DataFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      TechnicalAttribute("noisy", "tLabel", "tDefinition", "http://fims.tv/tLink", "fLabel", "fDefinition", "http://fims.tv/fLink"),
      CaptioningFormat("suitable subtitle", "fLabel", "fDefinition", "http://fims.tv/flink", "http://fims.tv/capsource", "en-gb"),
      List(AncillaryDataFormat(1, 2, 3, 4), AncillaryDataFormat(5, 6, 7, 8)))
      
  val containerFormat = ContainerFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      TechnicalAttribute("vacant", "tLabel", "tDefinition", "http://fims.tv/tLink", "fLabel", "fDefinition", "http://fims.tv/fLink"),
      ContainerFormatParameters("pot", "fLabel", "fDefinition", "http://fims.tv/fLink"))
}