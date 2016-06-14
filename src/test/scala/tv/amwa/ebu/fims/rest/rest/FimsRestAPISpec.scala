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

package tv.amwa.ebu.fims.rest.rest
import scala.collection.JavaConverters.setAsJavaSetConverter
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.WordSpec
import tv.amwa.ebu.fims.rest.commons.Loggable
import tv.amwa.ebu.fims.rest.rest.cxf.FimsRestCXFInstance
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.Client
import org.scalatest.junit.JUnitRunner
import com.sun.jersey.api.client.ClientResponse
import tv.amwa.ebu.fims.rest.model.capture._
import tv.amwa.ebu.fims.rest.model._
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.converter.FimsXML
import java.io.OutputStream
import tv.amwa.ebu.fims.rest.Constants
import tv.amwa.ebu.fims.rest.rest.message.JobXMLMessageBodyReader
import tv.amwa.ebu.fims.rest.converter.CaptureJobXMLConversionSpec
import java.util.UUID
import tv.amwa.ebu.fims.rest.rest.message.HTTPErrorXMLMessageBodyReader
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError
import tv.amwa.ebu.fims.rest.converter.BMObjectXMLConversionSpec
import tv.amwa.ebu.fims.rest.converter.ProfileXMLConversionSpec
import scala.xml.XML
import javax.ws.rs.core.MultivaluedMap
import com.sun.jersey.core.util.MultivaluedMapImpl
import tv.amwa.ebu.fims.rest.converter.XMLValidator
import tv.amwa.ebu.fims.rest.converter.BMContentXMLConversionSpec
import tv.amwa.ebu.fims.rest.converter.BMContentFormatXMLConversionSpec
import tv.amwa.ebu.fims.rest.converter.DescriptionXMLConversionSpec
import tv.amwa.ebu.fims.rest.rest.message.BMContentXMLMessageBodyReader

@RunWith(classOf[JUnitRunner])
class FimsRestAPISpec extends WordSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with Loggable {
  val port = 9000
  val fimsRestInstance = new FimsRestCXFInstance(port)
  val client = {
    val config = new DefaultClientConfig
    config.getSingletons().addAll(Set(JobXMLMessageBodyReader,BMContentXMLMessageBodyReader,HTTPErrorXMLMessageBodyReader).asJava)
    Client.create(config)
  }
  val api = client.resource("http://localhost:" + port + "/api")
  val captureJob = CaptureJob(
        ResourceParameters(
            UUID.fromString("c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")),
        BaseJobParameters(
            bmObjects = List(BMObjectXMLConversionSpec.testObject), 
            priority = Medium, 
            startJob = StartJobByTime(System.currentTimeMillis() + 86400000l)),
        CaptureJobParameters(
            profiles = List(ProfileXMLConversionSpec.captureProfile), 
            startProcess = StartProcessByNoWait(), 
    		stopProcess = StopProcessByDuration(Duration(NormalPlayTime(65123l))), 
    		sourceID = "http://www.quantel.com",
    		sourceType = Controllable) )
  val bmContent = BMContent(
      ResourceParameters(UUID.fromString("7a07a2a7-08ea-42a5-b1cb-9d395e2daa1a")),
      BMContentFormatXMLConversionSpec.contentFormat,
      DescriptionXMLConversionSpec.description)

  val printer = new scala.xml.PrettyPrinter(1000, 2)

  override def beforeAll = fimsRestInstance.start
  override def afterAll = fimsRestInstance.stop
  override def beforeEach = { Thread.sleep(100); api.path("content").delete ; api.path("job").delete }
  
  "The FIMS REST API for job resources" should {
    "return HTTP 204 -no content- when deleting all jobs." in {
      val response = api.path("job").delete(classOf[ClientResponse])
      response.getStatus should be (204)
      response.getLength should be (0)
    }
    
    "initially return HTTP 200 -OK- with an empty list of jobs." in {
      val response = api.path("job").get(classOf[ClientResponse])
      response.getStatus should be (200)
      val emptyJobs = XML.load(response.getEntityInputStream())
      emptyJobs should not be (null)
      (emptyJobs.prefix, emptyJobs.label) should be (("bms", "jobs"))
      (emptyJobs \ "@totalSize").text should be ("0")
    } 
    
    "alllow the creation of a capture job and return 201 -created- with the location." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val response = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      response.getStatus should be (201)
      response.getHeaders().get("Location").get(0) should be ("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")
      
      val returnedJob = response.getEntity(classOf[CaptureJobType])
      returnedJob should not be (null)
      returnedJob.resourceID should equal (captureJob.resourceID)
      returnedJob.revisionID should equal (Some("1"))
      returnedJob.location should equal (Some(new java.net.URI("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")))
      returnedJob.resourceCreationDate.isDefined should be (true)
      returnedJob.resourceModifiedDate.isDefined should be (true)
      returnedJob.extensionGroup.isDefined should be (false)
      returnedJob.baseParameters should have ( // TODO test more parameters
          'status (Some(JobStatusType.Queued)),
          'statusDescription (Some("Capture job added to queue.")))
      returnedJob.serviceParameters should have (
          'sourceID (captureJob.sourceID))
    }
    
    "allow the reading of a specific job after creation with status 200 -OK-." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)

      val getResponse = api.path("job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600").accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (200)
      val returnedJob = getResponse.getEntity(classOf[CaptureJobType])
      returnedJob should not be (null)
      returnedJob.resourceID should equal (captureJob.resourceID)
      returnedJob.revisionID should equal (Some("1"))
      returnedJob.location should equal (Some(new java.net.URI("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")))
      returnedJob.resourceCreationDate.isDefined should be (true)
      returnedJob.resourceModifiedDate.isDefined should be (true)
      returnedJob.extensionGroup.isDefined should be (false)
      returnedJob.baseParameters should have ( // TODO test more parameters
          'status (Some(JobStatusType.Queued)),
          'statusDescription (Some("Capture job added to queue.")))
      returnedJob.serviceParameters should have (
          'sourceID (captureJob.sourceID))
    }
    
    "produce a 404 -Not Found- if a job is not available, with a FIMS fault as an entity." in {
      val getResponse = api.path("job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600").accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (404)
      val returnedFault = getResponse.getEntity(classOf[HTTPError])
      returnedFault should not be (null)
      returnedFault should be (HTTPError(404,Fault(ErrorCodeType.DAT_S00_0003,Some("404 Not Found"),
          Some("Capture job with identifier urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600 was not found in the in memory store."),List())))
    }
    
    "produce a 409 -Conflict- if a request is made to create a job that already exists, with a FIMS fault message." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)

      val overwriteResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      overwriteResponse.getStatus should be (409)
      val returnedFault = overwriteResponse.getEntity(classOf[HTTPError])
      returnedFault should not be (null)
      returnedFault should be (HTTPError(409,Fault(ErrorCodeType.DAT_S00_0005,Some("409 Conflict"),Some("A capture job with identifier urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600 already exists."),List())))
    }

    "allow the update of a specific job after creation with status 200 -OK-." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)

      val updateJob = captureJob.copy(
          resourceParameters = ResourceParameters(captureJob.resourceID),
          baseParameters = BaseJobParameters(), 
          serviceParameters = CaptureJobParameters(stopProcess = StopProcessByDuration(Duration(NormalPlayTime(75124l)))))
      val updateString = printer.format(FimsXML.write(updateJob))
      val updateResponse = api.path("job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600").accept("application/xml")
      	.header("Content-Type", "application/xml").post(classOf[ClientResponse], updateString)
      updateResponse.getStatus should be (200)
      
      val updatedJob = updateResponse.getEntity(classOf[CaptureJobType])
      updatedJob should not be (null)
      updatedJob.stopProcess should equal(Some(StopProcessByDuration(Duration(NormalPlayTime(75124l)))))
    }
    
    "deny the update of a specific job with the wrong resource ID with a 400 -Bad Reqest- and a FIMS fault." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)

      val updateJob = captureJob.copy(
          resourceParameters = ResourceParameters(UUID.randomUUID),
          baseParameters = BaseJobParameters(), 
          serviceParameters = CaptureJobParameters(outPoint = SourceOutPointByDuration(Duration(Timecode("10:11:12;13")))))
      val updateString = printer.format(FimsXML.write(updateJob))
      val updateResponse = api.path("job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600").accept("application/xml")
      	.header("Content-Type", "application/xml").post(classOf[ClientResponse], updateString)
      updateResponse.getStatus should be (400)
      val returnedFault = updateResponse.getEntity(classOf[HTTPError])
      returnedFault should not be (null)
      returnedFault should be (HTTPError(400,Fault(ErrorCodeType.DAT_S00_0001,Some("400 Bad Request"),Some("On update, resource identiiers do not match."),List())))
    }
    
    "allow the creation of a single job and the default full listing of that job in the collection of all jobs with status -200- OK." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)
      
      val getResponse = api.path("job").accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (200)
      val jobList = XML.load(getResponse.getEntityInputStream())
      jobList should not be (null)
      println(printer.format(jobList))
      (jobList \ "@totalSize").text should be ("1")
      (jobList \ "@detail").text should be ("full")
      // TODO fix this XMLValidator.validate(printer.format(jobList.child(0)), "captureMedia-V1_1_0.xsd") should be (true)
    }
    
    "allow the creation of a single job and the explicit full listing of that job in the collection of all jobs with status -200- OK." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)
      
      val getResponse = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "full"); m}).accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (200)
      val jobList = XML.load(getResponse.getEntityInputStream())
      jobList should not be (null)
      println(printer.format(jobList))
      (jobList \ "@totalSize").text should be ("1")
      (jobList \ "@detail").text should be ("full")
      // TODO fix this  XMLValidator.validate(printer.format(jobList.child(0)), "captureMedia-V1_1_0.xsd") should be (true)
    }

    "allow the creation of a single job and the summary listing of that job in the collection of all jobs with status -200- OK." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)
      
      val getResponse = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "summary"); m}).accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (200)
      val jobList = XML.load(getResponse.getEntityInputStream())
      jobList should not be (null)
      println(printer.format(jobList))
      (jobList \ "@totalSize").text should be ("1")
      (jobList \ "@detail").text should be ("summary")
      // TODO fix this XMLValidator.validate(printer.format(jobList.child(0)), "captureMedia-V1_1_0.xsd") should be (true)
    }

    "allow the creation of a single job and the link listing of that job in the collection of all jobs with status -200- OK." in {
      val jobString = printer.format(FimsXML.write(captureJob))
      val postResponse = api.path("job").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      postResponse.getStatus should be (201)
      
      val getResponse = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "link"); m}).accept("application/xml").get(classOf[ClientResponse]) 
      getResponse.getStatus should be (200)
      val jobList = XML.load(getResponse.getEntityInputStream())
      jobList should not be (null)
      println(printer.format(jobList))
      (jobList \ "@totalSize").text should be ("1")
      (jobList \ "@detail").text should be ("link")
      XMLValidator.validate(printer.format(jobList.child(0)), "captureMedia-V1_1_0.xsd") should be (true)
    } 
  } 
  
  "The FIMS REST API for BM content resources" should {
    "return HTTP 204 -no content- when deleting all BM content." in {
      val response = api.path("content").delete(classOf[ClientResponse])
      response.getStatus should be (204)
      response.getLength should be (0)
    }
    
    "initially return HTTP 200 -OK- with an empty list of BM content." in {
      val response = api.path("content").get(classOf[ClientResponse])
      response.getStatus should be (200)
      val emptyJobs = XML.load(response.getEntityInputStream())
      emptyJobs should not be (null)
      (emptyJobs.prefix, emptyJobs.label) should be (("bms", "bmContents"))
      (emptyJobs \ "@totalSize").text should be ("0")
    }

    "alllow the creation of a bm content and return 201 -created- with the location." in {
      val jobString = printer.format(FimsXML.write(bmContent))
      val response = api.path("content").accept("application/xml").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobString)
      response.getStatus should be (201)
      response.getHeaders().get("Location").get(0) should be ("http://localhost:9000/api/content/" + bmContent.resourceID.toString)
      
      val returnedContent = response.getEntity(classOf[BMContentType])
      returnedContent should not be (null)
      println(printer.format(FimsXML.write(returnedContent)))
      returnedContent.resourceID should equal (bmContent.resourceID)
      returnedContent.revisionID should equal (Some("1"))
      returnedContent.location should equal (Some(new java.net.URI("http://localhost:9000/api/content/urn:uuid:7a07a2a7-08ea-42a5-b1cb-9d395e2daa1a")))
      returnedContent.resourceCreationDate.isDefined should be (true)
      returnedContent.resourceModifiedDate.isDefined should be (true)
      returnedContent.extensionGroup.isDefined should be (false)
    }

  } 

}