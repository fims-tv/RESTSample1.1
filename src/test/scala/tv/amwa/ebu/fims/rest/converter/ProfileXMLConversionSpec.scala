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
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfile
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfileType
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.BaseProfileParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureProfileParameters
import tv.amwa.ebu.fims.rest.model.TransferAtom
import tv.amwa.ebu.fims.rest.model.TransformAtom
import tv.amwa.ebu.fims.rest.model.transfer.TransferProfile
import tv.amwa.ebu.fims.rest.model.transfer.TransferProfileParameters
import tv.amwa.ebu.fims.rest.model.transform.TransformProfile
import tv.amwa.ebu.fims.rest.model.transform.TransformProfileParameters
import tv.amwa.ebu.fims.rest.model.transfer.TransferProfileType
import tv.amwa.ebu.fims.rest.model.transform.TransformProfileType

@RunWith(classOf[JUnitRunner])
class ProfileXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created capture profile" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val captureProfile = ProfileXMLConversionSpec.captureProfile
          
      val outNodes = FimsXML.write(captureProfile)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[CaptureProfileType](outNodes)
      println(captureProfile.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[CaptureProfileType](captureProfile) should equal (captureProfile)
      XMLRoundTrip.writeToBytesThenRead[CaptureProfileType](captureProfile) should equal (captureProfile)
    }
    
    "be validated against the FIMS schema" in {
      val captureProfile = ProfileXMLConversionSpec.captureProfile
      
      val outNodes = FimsXML.write(captureProfile)
      XMLValidator.validate(printer.format(outNodes), "captureMedia-V1_1_0.xsd") should be (true)
    }
  }
  
  "A manually created transfer profile" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val transferProfile = ProfileXMLConversionSpec.transferProfile
          
      val outNodes = FimsXML.write(transferProfile)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[CaptureProfileType](outNodes)
      println(transferProfile.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[TransferProfileType](transferProfile) should equal (transferProfile)
      XMLRoundTrip.writeToBytesThenRead[TransferProfileType](transferProfile) should equal (transferProfile)
    }
    
    "be validated against the FIMS schema" in {
      val transferProfile = ProfileXMLConversionSpec.transferProfile
      
      val outNodes = FimsXML.write(transferProfile)
      XMLValidator.validate(printer.format(outNodes), "transferMedia-V1_1_0.xsd") should be (true)
    }
  }

  "A manually created transform profile" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val transformProfile = ProfileXMLConversionSpec.transformProfile
          
      val outNodes = FimsXML.write(transformProfile)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[CaptureProfileType](outNodes)
      println(transformProfile.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[TransformProfileType](transformProfile) should equal (transformProfile)
      XMLRoundTrip.writeToBytesThenRead[TransformProfileType](transformProfile) should equal (transformProfile)
    }
    
    "be validated against the FIMS schema" in {
      val transformProfile = ProfileXMLConversionSpec.transformProfile
      
      val outNodes = FimsXML.write(transformProfile)
      XMLValidator.validate(printer.format(outNodes), "transformMedia-V1_1_0.xsd") should be (true)
    }
  }

}

object ProfileXMLConversionSpec {
  val captureProfile = CaptureProfile(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BaseProfileParameters(ServiceXMLConversionSpec.service, "capture me", "recording for ever"),
      CaptureProfileParameters(
          TransformAtom(FormatXMLConversionSpec.videoFormat, FormatXMLConversionSpec.audioFormat, FormatXMLConversionSpec.containerFormat), 
          List(TransferAtom("http://fims.tv/destination1"), TransferAtom("http://fims.tv/destination2")),
          "pattern"))
  val transferProfile = TransferProfile(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BaseProfileParameters(ServiceXMLConversionSpec.service, "transfer me", "moving on"),
      TransferProfileParameters(List(TransferAtom("http://fims.tv/destination1"), TransferAtom("http://fims.tv/destination2"))))
  val transformProfile = TransformProfile(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      BaseProfileParameters(ServiceXMLConversionSpec.service, "transfer me", "moving on"),
      TransformProfileParameters(
          TransformAtom(FormatXMLConversionSpec.videoFormat, FormatXMLConversionSpec.audioFormat, FormatXMLConversionSpec.containerFormat), 
          List(TransferAtom("http://fims.tv/destination1"), TransferAtom("http://fims.tv/destination2")),
          "pattern"))
}