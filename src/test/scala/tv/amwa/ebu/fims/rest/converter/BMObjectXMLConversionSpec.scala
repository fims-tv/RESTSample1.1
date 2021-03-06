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
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.BMContent
import tv.amwa.ebu.fims.rest.model.BMObject
import tv.amwa.ebu.fims.rest.model.BMObjectType

@RunWith(classOf[JUnitRunner])
class BMObjectXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created bm object" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val testObject = BMObjectXMLConversionSpec.testObject
      
      val outNodes = FimsXML.write(testObject)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[BMObjectType](outNodes)
      println(testObject.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[BMObjectType](testObject) should equal (testObject)
      XMLRoundTrip.writeToBytesThenRead[BMObjectType](testObject) should equal (testObject)
    }
    
    "be validated against the FIMS schema" in {
      val testObject = BMObjectXMLConversionSpec.testObject
      
      val outNodes = FimsXML.write(testObject)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object BMObjectXMLConversionSpec {
  val testObject = BMObject(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BMContentXMLConversionSpec.content)
}