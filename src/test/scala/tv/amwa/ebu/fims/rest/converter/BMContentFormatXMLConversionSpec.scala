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
import tv.amwa.ebu.fims.rest.model.BMContentFormat
import tv.amwa.ebu.fims.rest.model.BMContentFormatType
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.SimpleFileLocator
import tv.amwa.ebu.fims.rest.model.Storage
import tv.amwa.ebu.fims.rest.model.ContainerMimeType
import tv.amwa.ebu.fims.rest.model.ListFileLocator
import tv.amwa.ebu.fims.rest.model.FormatCollection
import tv.amwa.ebu.fims.rest.model.Duration
import tv.amwa.ebu.fims.rest.model.Timecode
import tv.amwa.ebu.fims.rest.model.HashFunctionTypes
import tv.amwa.ebu.fims.rest.model.CRC32
import tv.amwa.ebu.fims.rest.model.Hash
import tv.amwa.ebu.fims.rest.model.MimeType

@RunWith(classOf[JUnitRunner])
class BMContentFormatXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)

  "A manually created bm content format" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val contentFormat = BMContentFormatXMLConversionSpec.contentFormat
      
      val outNodes = FimsXML.write(contentFormat)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[BMContentFormatType](outNodes)
      println(contentFormat.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[BMContentFormatType](contentFormat) should equal (contentFormat)
      XMLRoundTrip.writeToBytesThenRead[BMContentFormatType](contentFormat) should equal (contentFormat)
    }

    "be validated against the FIMS schema" in {
      val contentFormat = BMContentFormatXMLConversionSpec.contentFormat
      
      val outNodes = FimsXML.write(contentFormat)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object BMContentFormatXMLConversionSpec {
  val contentFormat = BMContentFormat(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BMEssenceLocatorXMLConversionSpec.simpleFileLocator,
      FormatCollection(FormatXMLConversionSpec.videoFormat, FormatXMLConversionSpec.audioFormat, FormatXMLConversionSpec.dataFormat, FormatXMLConversionSpec.containerFormat),
      Duration(Timecode("12:13:14:15")),
      Hash(CRC32,"1a2b3c4d"),
      System.currentTimeMillis,
      MimeType("application/mxf", "one", "two", "http://3.com"))
}