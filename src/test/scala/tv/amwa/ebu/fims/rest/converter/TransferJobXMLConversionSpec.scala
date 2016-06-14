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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers
import tv.amwa.ebu.fims.rest.model.transfer.TransferJob
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import java.util.UUID
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import tv.amwa.ebu.fims.rest.model.AsyncEndpoint
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.JobStatusType
import tv.amwa.ebu.fims.rest.model.Queue
import tv.amwa.ebu.fims.rest.model.Medium
import tv.amwa.ebu.fims.rest.model.StartJobByTime
import tv.amwa.ebu.fims.rest.model.ProcessedInfoByBytes
import tv.amwa.ebu.fims.rest.model.transfer.TransferJobParameters
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.model.transfer.TransferJobType

@RunWith(classOf[JUnitRunner])
class TransferJobXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created transfer job " should {
    "be serialized to XML and de-serialized back to a case class" in {
      val transferJob = TransferJobXMLConversionSpec.transferJob
      
      val outNodes = FimsXML.write(transferJob)
      println(printer.format(outNodes))
      
      val inValue = FimsXML.read[TransferJobType](outNodes)
      println(transferJob.toString)
      println(inValue.toString)
      
      XMLRoundTrip.writewriteNodeThenRead[TransferJobType](transferJob) should equal (transferJob)
      XMLRoundTrip.writeToBytesThenRead[TransferJobType](transferJob) should equal (transferJob)
    }
    "be validated against the FIMS schema" in {
      val transferJob = TransferJobXMLConversionSpec.transferJob
      
      val outNodes = FimsXML.write(transferJob)
      XMLValidator.validate(printer.format(outNodes), "transferMedia-V1_1_0.xsd") should be (true) 
    }
  }
}

object TransferJobXMLConversionSpec {
    val transferJob = TransferJob(
        ResourceParameters(
            UUIDBasedResourceID(UUID.randomUUID), "1", "http://fims.tv/location",
    	    System.currentTimeMillis - 3600000l, Some(System.currentTimeMillis),
    		AsyncEndpoint("http://fims.tv/reply", "http://fims.tv/fault") ),
        BaseJobParameters(JobStatusType.Queued, "A transfer job", "serviceID", 
            Queue(ResourceParameters(UUIDBasedResourceID(UUID.randomUUID))), Nil, "transfer", // TODO test tasks
            BMObjectXMLConversionSpec.testObject, Medium, 
            StartJobByTime(System.currentTimeMillis()), System.currentTimeMillis + 3600000l,
    		3600000l, 0, System.currentTimeMillis() + 60000l, 120000l, System.currentTimeMillis + 3600000l,
    		ProcessedInfoByBytes(50, 500000000l)),
    	TransferJobParameters(ProfileXMLConversionSpec.transferProfile))
}