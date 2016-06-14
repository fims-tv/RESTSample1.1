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
import tv.amwa.ebu.fims.rest.model.Service
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.ServiceType
import scala.xml.Text
import scala.xml.NodeSeq

@RunWith(classOf[JUnitRunner])
class ServiceXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)

  "A manually created service" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val service = ServiceXMLConversionSpec.service
          
      val outNodes = FimsXML.write(service)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[ServiceType](outNodes)
      println(service.toString)
      println(inValue.toString + "\n")
            
      XMLRoundTrip.writewriteNodeThenRead[ServiceType](service) should equal (service)
      XMLRoundTrip.writeToBytesThenRead[ServiceType](service) should equal (service)
    }

    "be validated against the FIMS schema" in {
      val service = ServiceXMLConversionSpec.service
      
      val outNodes = FimsXML.write(service)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object ServiceXMLConversionSpec {
  val service = Service(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      "FIMS Services Inc",
      "http://fims.tv/serrvices", // TODO improve the creation of service descriptions
      None) //Some(List(<an:other xmlns:an="http://fims.tv/another">{Text("description")}</an:other>)))
}