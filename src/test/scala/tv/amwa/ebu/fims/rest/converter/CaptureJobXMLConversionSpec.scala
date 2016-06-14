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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.capture.Controllable
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.AsyncEndpoint
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import tv.amwa.ebu.fims.rest.model.Medium
import tv.amwa.ebu.fims.rest.model.StartJobByTime
import tv.amwa.ebu.fims.rest.model.ProcessedInfoByBytes
import tv.amwa.ebu.fims.rest.model.StartProcessByNoWait
import tv.amwa.ebu.fims.rest.model.StopProcessByDuration
import tv.amwa.ebu.fims.rest.model.NormalPlayTime
import tv.amwa.ebu.fims.rest.model.Duration
import tv.amwa.ebu.fims.rest.model.capture.SourceInPointByBeginning
import tv.amwa.ebu.fims.rest.model.capture.SourceOutPointByDuration
import tv.amwa.ebu.fims.rest.model.Timecode
import tv.amwa.ebu.fims.rest.model.Queue
import tv.amwa.ebu.fims.rest.model.JobStatusType

@RunWith(classOf[JUnitRunner])
class CaptureJobXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created capture job " should {

    "be serialized to XML and de-serialized back to a case class" in {
      val captureJob = CaptureJobXMLConversionSpec.captureJob
      
      val outNodes = FimsXML.write(captureJob)
      println(printer.format(outNodes))
      val inValue = FimsXML.read[CaptureJobType](outNodes)
      println(captureJob.toString)
      println(inValue.toString)
      
      XMLRoundTrip.writewriteNodeThenRead[CaptureJobType](captureJob) should equal (captureJob)
      XMLRoundTrip.writeToBytesThenRead[CaptureJobType](captureJob) should equal (captureJob)
    }
    
    "be validated against the FIMS schema" in {
      val captureJob = CaptureJobXMLConversionSpec.captureJob
      
      val outNodes = FimsXML.write(captureJob)
      XMLValidator.validate(printer.format(outNodes), "captureMedia-V1_1_0.xsd") should be (true)
    }
  }

//  "A shim with arbitraty data" should {
//    "be serialized to XML and de-serialized back to a case class" in {
//      forAll((Generators.XMLString, "Shim name"), (Generators.setOfXMLStrings, "Annotations")) { (name: String, annotations: Set[String]) =>
//        val shim = Shim(name = name, ID = new URI("http://www.quantel.com"), annotations = annotations)
//        XMLRoundTrip.writewriteNodeThenRead(shim) should equal (shim)
//        XMLRoundTrip.writeToBytesThenRead(shim) should equal (shim)
//      }
//    }
//  }
}

object CaptureJobXMLConversionSpec {
    val captureJob = CaptureJob(
        ResourceParameters(
            UUIDBasedResourceID(UUID.randomUUID), "1", "http://fims.tv/location",
    	    System.currentTimeMillis - 3600000l, Some(System.currentTimeMillis),
    		AsyncEndpoint("http://fims.tv/reply", "http://fims.tv/fault") ),
        BaseJobParameters(JobStatusType.Queued, "A capture job", "serviceID", 
            Queue(ResourceParameters(UUIDBasedResourceID(UUID.randomUUID))), Nil, "capture", // TODO test tasks
            BMObjectXMLConversionSpec.testObject, Medium, 
            StartJobByTime(System.currentTimeMillis()), System.currentTimeMillis + 3600000l,
    		3600000l, 0, System.currentTimeMillis() + 60000l, 120000l, System.currentTimeMillis + 3600000l,
    		ProcessedInfoByBytes(50, 500000000l)),
        CaptureJobParameters(
            ProfileXMLConversionSpec.captureProfile, 
            StartProcessByNoWait(), 
    		StopProcessByDuration(Duration(NormalPlayTime(65123l))), 
    		"http://www.quantel.com", Controllable, SourceInPointByBeginning(),
    		SourceOutPointByDuration(Duration(Timecode("00:11:22:12"))), false)
      )
}