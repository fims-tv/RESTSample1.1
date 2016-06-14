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
import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.Fault
import tv.amwa.ebu.fims.rest.model.InnerFault
import scala.xml.NodeSeq
import tv.amwa.ebu.fims.rest.model.capture.CaptureFault
import tv.amwa.ebu.fims.rest.model.capture.CaptureFaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import tv.amwa.ebu.fims.rest.model.capture.CaptureErrorCodeType

@RunWith(classOf[JUnitRunner])
class FaultXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created fault" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val fault = FaultXMLConversionSpec.fault
      
      val outNodes = FimsXML.write(fault)
      println(printer.format(outNodes))
      val inValue = FimsXML.read(outNodes)(FaultConverter)
      println(fault.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[FaultType](fault)(FaultConverter, FaultConverter) should equal (fault)
      XMLRoundTrip.writeToBytesThenRead[FaultType](fault)(FaultConverter, FaultConverter) should equal (fault)
    }
  
    "be validated against the FIMS schema" in {
      val fault = FaultXMLConversionSpec.fault
      
      val outNodes = FimsXML.write(fault)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
  
  "A manually created capture fault" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
      val fault = FaultXMLConversionSpec.captureFault
      
      val outNodes = FimsXML.write[CaptureFaultType](fault)(CaptureFaultConverter)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[CaptureFaultType](outNodes)
      println(fault.toString)
      println(inValue.toString + "\n")
      
      XMLRoundTrip.writewriteNodeThenRead[CaptureFaultType](fault)(CaptureFaultConverter, CaptureFaultConverter) should equal (fault)
      XMLRoundTrip.writeToBytesThenRead[CaptureFaultType](fault)(CaptureFaultConverter, CaptureFaultConverter) should equal (fault)
    }
  
    "be validated against the FIMS schema" in {
      val fault = FaultXMLConversionSpec.captureFault
      
      val outNodes = FimsXML.write[CaptureFaultType](fault)(CaptureFaultConverter)
      XMLValidator.validate(printer.format(outNodes), "captureMedia-V1_0_7.xsd") should be (true)
    }
  }

}

object FaultXMLConversionSpec {
  val fault = Fault(
      ErrorCodeType.SVC_S00_0016, "problem", "Stopped working", 
      InnerFault("local-code", "broken", "not the expected outcome"))
  val captureFault = CaptureFault(
      ErrorCodeType.EXT_S00_0000, "capture-problem", "dropped catch",
      List(InnerFault("local-code", "broken", "not the expected outcome"), InnerFault("local-code", "broken", "not the expected outcome")),
      CaptureErrorCodeType.SVC_S03_0002)
}