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
import tv.amwa.ebu.fims.rest.model.BMContent
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.BMContentType

@RunWith(classOf[JUnitRunner])
class BMContentXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created bm content" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val content = BMContentXMLConversionSpec.content
      
      val outNodes = FimsXML.write(content)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[BMContentType](outNodes)
      println(content.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[BMContentType](content) should equal (content)
      XMLRoundTrip.writeToBytesThenRead[BMContentType](content) should equal (content)
    }

    "be validated against the FIMS schema" in {
      val content = BMContentXMLConversionSpec.content
      
      val outNodes = FimsXML.write(content)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object BMContentXMLConversionSpec {
  val content = BMContent(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BMContentFormatXMLConversionSpec.contentFormat,
      DescriptionXMLConversionSpec.description
  )
}