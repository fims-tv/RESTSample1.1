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
import tv.amwa.ebu.fims.rest.model.Description
import tv.amwa.ebu.fims.rest.model.BMContentDescription
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.DescriptionType
import tv.amwa.ebu.fims.rest.model.BMContentDescription
import tv.amwa.ebu.fims.rest.model.TextElement
import tv.amwa.ebu.fims.rest.model.Title
import tv.amwa.ebu.fims.rest.model.ContentDescription
import tv.amwa.ebu.fims.rest.model.Identifier

@RunWith(classOf[JUnitRunner])
class DescriptionXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created description" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val description = DescriptionXMLConversionSpec.description
      
      val outNodes = FimsXML.write(description)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[DescriptionType](outNodes)
      println(description.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[DescriptionType](description) should equal (description)
      XMLRoundTrip.writeToBytesThenRead[DescriptionType](description) should equal (description)
    }
    
    "be validated against the FIMS schema" in {
      val description = DescriptionXMLConversionSpec.description
      
      val outNodes = FimsXML.write(description)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object DescriptionXMLConversionSpec {
  val description = Description(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BMContentDescription(
          title = List(TextElement("Fims"), TextElement("AMWA", "en-gb")),
          alternativeTitle = List(Title("EBU"), Title("SMPTE", "en-us", "label", "definition", "http://link.com", "en-us")),
          description = List(ContentDescription("Media devices work in harmony.", "fr", "label", "definition", "http://link.com", "en-gb")),
          identifier = List(Identifier("urn:uuid:"+UUID.randomUUID.toString, "typelabel", "typedefinition", "http://typelink.com", "ty-pe",
              "formatlabel", "formatdefinition", "http://formatlink.com", "fr-mt"),
            Identifier("urn:uuid:"+UUID.randomUUID.toString, "typelabel", "typedefinition", "http://typelink.com", "ty-pe",
              "formatlabel", "formatdefinition", "http://formatlink.com", "fr-mt")),
          version = "repeats",
          lang = "en-us"))
}