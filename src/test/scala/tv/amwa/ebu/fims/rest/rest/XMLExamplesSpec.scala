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
import tv.amwa.ebu.fims.rest.rest.message.JobXMLMessageBodyReader
import tv.amwa.ebu.fims.rest.rest.message.BMContentXMLMessageBodyReader
import tv.amwa.ebu.fims.rest.rest.message.HTTPErrorXMLMessageBodyReader
import tv.amwa.ebu.fims.rest.model.capture._
import tv.amwa.ebu.fims.rest.model._
import tv.amwa.ebu.fims.rest.converter.ParameterConverters._
import tv.amwa.ebu.fims.rest.converter.XMLConverters._
import tv.amwa.ebu.fims.rest.converter.StringConverters._
import tv.amwa.ebu.fims.rest.rest.converter.XMLConverters.HTTPErrorConverter
import tv.amwa.ebu.fims.rest.converter.FimsXML
import tv.amwa.ebu.fims.rest.converter.FimsString
import java.util.UUID
import scala.xml._
import com.sun.jersey.api.client.ClientResponse
import tv.amwa.ebu.fims.rest.rest.handling.HTTPError
import tv.amwa.ebu.fims.rest.rest.jersey.FimsRestJerseyInstance
import com.sun.jersey.core.util.MultivaluedMapImpl
import tv.amwa.ebu.fims.rest.converter.XMLNamespaceProcessor
import tv.amwa.ebu.fims.rest.Namespaces

@RunWith(classOf[JUnitRunner])
class XMLExamplesSpec extends WordSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with Loggable {
  val port = 9000
  val fimsRestInstance = new FimsRestCXFInstance(port)
//  val fimsRestInstance = new FimsRestJerseyInstance(port)
  val client = {
    val config = new DefaultClientConfig
    config.getSingletons().addAll(Set(JobXMLMessageBodyReader,BMContentXMLMessageBodyReader,HTTPErrorXMLMessageBodyReader).asJava)
    Client.create(config)
  }
  val api = client.resource("http://localhost:" + port + "/api")
  val printer = new scala.xml.PrettyPrinter(80, 2)

  override def beforeAll = fimsRestInstance.start
  override def afterAll = fimsRestInstance.stop
  override def beforeEach = { Thread.sleep(100); api.path("content").delete ; api.path("job").delete }

  val contentPlaceholder = BMContent(
      ResourceParameters(UUID.fromString("11b5293f-1be1-445a-997a-caa6f2b2bee4")),
      Nil, // No formats ... these are maintained
      List(Description(
          ResourceParameters(
              resourceID = UUID.fromString("3e4Faa49-97c9-49bc-9db2-41af36d67d2b"),
              extensionGroup = <isa:Attributes xmlns:isa="http://isa.quantel.com">
          		  <isa:Title>{Text("Games of the century")}</isa:Title>
                  <isa:Category>{Text("NTSC")}</isa:Category>
              	  <isa:Owner>{Text("FIMS History Channel")}</isa:Owner>
              	  <isa:Description>{Text("Best games of the past 100 years.")}</isa:Description>
              	</isa:Attributes>))))
  
  def postContentPlaceholder(placeholder: BMContent) = {
      val contentPlaceholderAsXML = printer.format(FimsXML.write(placeholder))
      val response = api.path("content").accept("application/xml").header("Content-Type", "application/xml")
      	.header("X-FIMS-Version", "1.1").post(classOf[ClientResponse], contentPlaceholderAsXML)
      response.getStatus should be (201)
      response.getHeaders().get("Location").get(0) should be ("http://localhost:9000/api/content/" + FimsString.write(placeholder.resourceID))
      response.getHeaders().get("X-FIMS-Version").get(0) should be ("1.1")
      response
  }
  
  // Simulated built in profiles
  val nativeProfile = UUID.fromString("7913953e-6155-423f-90bb-d3e60db57291")
  val lbrProfile = UUID.fromString("b1ff36db-cd1d-4e5e-b6db-1571e5e239c2")
  val hbrProfile = UUID.fromString("30bdb5ca-66e5-4b7a-b2fd-23f9569e82e2")
  
  def captureJob = CaptureJob( // Use a function so that the job is always in the future
        ResourceParameters(
            UUID.fromString("c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")),
        BaseJobParameters(
            bmObjects = List(BMObject(ResourceParameters(
                resourceID = UUID.randomUUID,
                extensionGroup = <myco:contentIDs xmlns:myco="http://myco.com/contentID">
                    <myco:feedID>{Text(contentPlaceholder.resourceID.toString)}</myco:feedID>
                    <myco:recordID>urn:uuid:5d3b6b4a-aa22-4557-b675-90ea1d6deb53</myco:recordID>
                  </myco:contentIDs>), 
                  BMContent(ResourceParameters(contentPlaceholder.resourceID), Nil, Nil))), 
            priority = Medium, 
            startJob = StartJobByTime(System.currentTimeMillis() + 20000l)),
        CaptureJobParameters(
            profiles = List(CaptureProfile(ResourceParameters(hbrProfile), BaseProfileParameters(), CaptureProfileParameters())), 
            startProcess = StartProcessByNoWait(), 
    		stopProcess = StopProcessByDuration(Duration(NormalPlayTime(60000l))), 
    		sourceID = "rtp://224.0.0.1:5000/",
    		sourceType = Uncontrolled) )
  
  def postCaptureJob(what: CaptureJob) = {
      postContentPlaceholder(contentPlaceholder)
      val whatAsXML = printer.format(FimsXML.write(what))
      val response = api.path("job").accept("application/xml").header("Content-Type", "application/xml")
        .header("X-FIMS-Version", "1.1").post(classOf[ClientResponse], whatAsXML)
      response.getStatus should be (201)
      response.getHeaders().get("Location").get(0) should be ("http://localhost:9000/api/job/" + FimsString.write(what.resourceID))
      response.getHeaders().get("X-FIMS-Version").get(0) should be ("1.1")
      response
  }
  
  "The FIMS REST API for content placeholders" should {
    "allow the creation of a BMContent content placeholder with simple metadata" ignore {
      val response = postContentPlaceholder(contentPlaceholder)

      val contentPlaceholderResponseBody = response.getEntity(classOf[BMContentType])
      contentPlaceholderResponseBody should not be (null)
      println("*** Creating placeholders - response\n" + printer.format(FimsXML.write(contentPlaceholderResponseBody)))
      contentPlaceholderResponseBody.resourceID should equal (contentPlaceholder.resourceID)
      contentPlaceholderResponseBody.revisionID should equal (Some("1"))
      contentPlaceholderResponseBody.location should equal (Some(new java.net.URI("http://localhost:9000/api/content/urn:uuid:11b5293f-1be1-445a-997a-caa6f2b2bee4")))
      contentPlaceholderResponseBody.resourceCreationDate.isDefined should be (true)
      contentPlaceholderResponseBody.resourceModifiedDate.isDefined should be (true)
      contentPlaceholderResponseBody.extensionGroup.isDefined should be (false)
      contentPlaceholderResponseBody.descriptions(0).extensionGroup.isDefined should be (true)
      val extensionGroup = contentPlaceholderResponseBody.descriptions(0).extensionGroup.get
      (extensionGroup \ "Title").text should equal ("Games of the century")
      (extensionGroup \ "Category").text should equal ("NTSC")
      (extensionGroup \ "Owner").text should equal ("FIMS History Channel")
      (extensionGroup \ "Description").text should equal ("Best games of the past 100 years.")
    }
    
    "respond with a 400 -bad request- response for badly formatted XML" ignore {
      val contentPlaceholderAsXML = printer.format(FimsXML.write(contentPlaceholder)).substring(42)
      val response = api.path("content").accept("application/xml").header("Content-Type", "application/xml")
        .header("X-FIMS-Version", "1.1").post(classOf[ClientResponse], contentPlaceholderAsXML)
      response.getStatus should be (400)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val faultResponseBody = response.getEntity(classOf[HTTPError])
      faultResponseBody should not be (null)
      println("*** General fault example\n" + printer.format(FimsXML.write(faultResponseBody))) 
      faultResponseBody.statusCode should be (400)
      faultResponseBody.fault.code should equal (ErrorCodeType.DAT_S00_0001)
      faultResponseBody.fault.description should equal (Some("400 Bad Request"))
      faultResponseBody.fault.detail.isDefined should be (true)
      faultResponseBody.fault.innerFault should be (Nil)
    }
    
    "respond with a 409 -conflict- response for an attempt to create two placeholders with the same ID" ignore {
      postContentPlaceholder(contentPlaceholder)

      val contentPlaceholderAsXML = printer.format(FimsXML.write(contentPlaceholder))
      val response = api.path("content").accept("application/xml").header("Content-Type", "application/xml")
        .header("X-FIMS-Version", "1.1").post(classOf[ClientResponse], contentPlaceholderAsXML)
      response.getStatus should be (409)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
     
      val faultResponseBody = response.getEntity(classOf[HTTPError])
      faultResponseBody should not be (null)
      println("*** FIMS conventions - 409 conflict\n" + printer.format(FimsXML.write(faultResponseBody))) 
      faultResponseBody.statusCode should be (409)
      faultResponseBody.fault.code should equal (ErrorCodeType.DAT_S00_0005)
      faultResponseBody.fault.description should equal (Some("409 Conflict"))
      faultResponseBody.fault.detail.isDefined should be (true)
      faultResponseBody.fault.innerFault should be (Nil)
    }
    
    "allow a single BMContent placeholder to be queried" ignore {
      postContentPlaceholder(contentPlaceholder)
    
      val response = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val contentPlaceholderResponseBody = response.getEntity(classOf[BMContentType])
      contentPlaceholderResponseBody should not be (null)
      println("*** Querying one placeholder - response\n" + printer.format(FimsXML.write(contentPlaceholderResponseBody)))
      contentPlaceholderResponseBody.resourceID should equal (contentPlaceholder.resourceID)
      contentPlaceholderResponseBody.revisionID should equal (Some("1"))
      contentPlaceholderResponseBody.location should equal (Some(new java.net.URI("http://localhost:9000/api/content/urn:uuid:11b5293f-1be1-445a-997a-caa6f2b2bee4")))
      contentPlaceholderResponseBody.resourceCreationDate.isDefined should be (true)
      contentPlaceholderResponseBody.resourceModifiedDate.isDefined should be (true)
      contentPlaceholderResponseBody.extensionGroup.isDefined should be (false)
      contentPlaceholderResponseBody.descriptions(0).extensionGroup.isDefined should be (true)
      val extensionGroup = contentPlaceholderResponseBody.descriptions(0).extensionGroup.get
      (extensionGroup \ "Title").text should equal ("Games of the century")
      (extensionGroup \ "Category").text should equal ("NTSC")
      (extensionGroup \ "Owner").text should equal ("FIMS History Channel")
      (extensionGroup \ "Description").text should equal ("Best games of the past 100 years.")
    }
    
    "report a 404 status fault response if an unknown bmContent is queried" ignore {
      postContentPlaceholder(contentPlaceholder)
       
      val response = api.path("content/urn:uuid:" + UUID.randomUUID.toString)
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (404)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val faultResponseBody = response.getEntity(classOf[HTTPError])
      faultResponseBody should not be (null)
      println("*** Queryng one placeholder - 404 fault\n" + printer.format(FimsXML.write(faultResponseBody))) 
      faultResponseBody.statusCode should be (404)
      faultResponseBody.fault.code should equal (ErrorCodeType.DAT_S00_0003)
      faultResponseBody.fault.description should equal (Some("404 Not Found"))
      faultResponseBody.fault.detail.isDefined should be (true)
      faultResponseBody.fault.innerFault should be (Nil)
    }

    "list BMContent placeholders" ignore {
      postContentPlaceholder(contentPlaceholder)
       
      val response = api.path("content").accept("application/xml").header("X-FIMS-Version", "1.1").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val listResponse = XML.load(response.getEntityInputStream)
      println("*** Listing Placeholder\n" + printer.format(listResponse))
      listResponse.head.label should equal("bmContents")
      (listResponse \ "@totalSize").text should equal ("1")
      (listResponse \ "@detail").text should equal ("full")
      (listResponse \ "bmContent" \ "resourceID").text should equal (FimsString.write(contentPlaceholder.resourceID))
    }
    
    "list full BMContent placeholders" ignore {
      postContentPlaceholder(contentPlaceholder)
       
      val response = api.path("content").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "full"); m})
      	.header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val listResponse = XML.load(response.getEntityInputStream)
      println("*** Listing Placeholder - full\n" + printer.format(listResponse))
      listResponse.head.label should equal("bmContents")
      (listResponse \ "@totalSize").text should equal ("1")
      (listResponse \ "@detail").text should equal ("full")
      (listResponse \ "bmContent" \ "resourceID").text should equal (FimsString.write(contentPlaceholder.resourceID))
    }

    "list summary BMContent placeholders" ignore {
      postContentPlaceholder(contentPlaceholder)
       
      val response = api.path("content").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "summary"); m})
      	.header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val listResponse = XML.load(response.getEntityInputStream)
      println("*** Listing Placeholder - summary\n" + printer.format(listResponse))
      listResponse.head.label should equal("bmContents")
      (listResponse \ "@totalSize").text should equal ("1")
      (listResponse \ "@detail").text should equal ("summary")
      (listResponse \ "bmContent" \ "resourceID").text should equal (FimsString.write(contentPlaceholder.resourceID))
    }

    "list link BMContent placeholders" ignore {
      postContentPlaceholder(contentPlaceholder)
       
      val response = api.path("content").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "link"); m})
      	.header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val listResponse = XML.load(response.getEntityInputStream)
      println("*** Listing Placeholder - link\n" + printer.format(listResponse))
      listResponse.head.label should equal("bmContents")
      (listResponse \ "@totalSize").text should equal ("1")
      (listResponse \ "@detail").text should equal ("link")
      (listResponse \ "bmContent" \ "resourceID").text should equal (FimsString.write(contentPlaceholder.resourceID))
    }
    
    "modify a placeholder by overriding existing values" ignore {
      postContentPlaceholder(contentPlaceholder)
      val updateDetails: BMContent = BMContent(ResourceParameters(UUID.fromString("11b5293f-1be1-445a-997a-caa6f2b2bee4")), Nil, 
          List(Description(
            ResourceParameters(
              resourceID = UUID.fromString("3e4Faa49-97c9-49bc-9db2-41af36d67d2b"),
              extensionGroup = <isa:Attributes xmlns:isa="http://isa.quantel.com">
          		  <isa:Title>{Text("Games of the century")}</isa:Title>
                  <isa:Category>{Text("NTSC")}</isa:Category>
              	  <isa:Owner>{Text("FIMS History Channel")}</isa:Owner>
              	  <isa:Description>{Text("Digest of the past 100 years.")}</isa:Description>
              	</isa:Attributes>))))
      val updateDetailsAsXML = printer.format(FimsXML.write(updateDetails))
      println("*** Modifying a placeholder - overwrite request\n" + updateDetailsAsXML)
      val response = api.path("content/" + FimsString.write(contentPlaceholder.resourceID)).accept("application/xml")
        .header("Content-Type", "application/xml").header("X-FIMS-Version", "1.1").post(classOf[ClientResponse], updateDetailsAsXML)
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val updatedContentResponse = response.getEntity(classOf[BMContent])
      updatedContentResponse should not be (null)
      println("*** Modifying a placeholder - overwrite response\n" + printer.format(FimsXML.write(updatedContentResponse)))
      updatedContentResponse.revisionID should equal (Some("2"))
      updatedContentResponse.descriptions(0).extensionGroup.isDefined should be (true)
      val extensionGroup = updatedContentResponse.descriptions(0).extensionGroup.get
      (extensionGroup \ "Title").text should equal ("Games of the century")
      (extensionGroup \ "Category").text should equal ("NTSC")
      (extensionGroup \ "Owner").text should equal ("FIMS History Channel")
      (extensionGroup \ "Description").text should equal ("Digest of the past 100 years.")     
    }
    
    "delete a single placeholder" ignore {
      postContentPlaceholder(contentPlaceholder)
      
      val getResponse1 = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse1.getStatus should be (200)
      getResponse1.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val response = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").delete(classOf[ClientResponse])
      response.getStatus should be (204)
      response.getLength should be (0)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val getResponse2 = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse2.getStatus should be (404) 
      getResponse2.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
    }
    
    // TODO test that it is not possible to delete a placeholder referenced by a capture job
    
    "purge all unreferenced placeholders" ignore {
      postContentPlaceholder(contentPlaceholder)

      val getResponse1 = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse1.getStatus should be (200)
      getResponse1.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val response = api.path("content/purge").header("X-FIMS-Version", "1.1").post(classOf[ClientResponse])
      response.getStatus should be (204)
      response.getLength should be (0)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val getResponse2 = api.path("content/" + FimsString.write(contentPlaceholder.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse2.getStatus should be (404) 
      getResponse2.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")      
    }
    
    // TODO test the complete repository API
  } 
  
  "The FIMS REST API for capture jobs" should {
    "allow the creation and execution of a short capture job" ignore {
      val testJob = captureJob
      println("*** Registering a new job - request\n" + printer.format(FimsXML.write(testJob)))
      val response = postCaptureJob(testJob)
      response should not be (null)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
    
//      println(response.getEntity(classOf[java.lang.String]).replace("><", ">\n<"))
      // TODO problem with repeating namespaces inside extension elements
      val testJobResponse = response.getEntity(classOf[CaptureJob])
      println("*** Registering a new job - response\n" + printer.format(FimsXML.write(testJobResponse)))
      testJobResponse.resourceID should equal (captureJob.resourceID)
      testJobResponse.revisionID should equal (Some("1"))
      testJobResponse.location should equal (Some(new java.net.URI("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")))
      testJobResponse.resourceCreationDate.isDefined should be (true)
      testJobResponse.resourceModifiedDate.isDefined should be (true)
      testJobResponse.extensionGroup.isDefined should be (false)
  }
    
    "provide examples of start job kinds" ignore {
      val exampleJob = captureJob
      println("*** Setting the start job parameter - by no wait\n" + 
          printer.format((FimsXML.write(exampleJob.copy(baseParameters = exampleJob.baseParameters.copy(startJob = Some(StartJobByNoWait())))) \ "startJob").head))
      println("*** Setting the start job parameter - by time\n" + 
          printer.format((FimsXML.write(exampleJob) \ "startJob").head))
      println("*** Setting the start job parameter - by latest\n" + 
          printer.format((FimsXML.write(exampleJob.copy(baseParameters = exampleJob.baseParameters.copy(startJob = Some(StartJobByLatest())))) \ "startJob").head))
    }
    
    "provide examples of start process kinds" in {
      val exampleJob = captureJob
      println("*** Setting the start process parameter - by no wait\n" + 
          printer.format((FimsXML.write(exampleJob) \ "startProcess").head))
      println("*** Setting the start job parameter - by time\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              startProcess = Some(StartProcessByTime(System.currentTimeMillis))))) \ "startProcess").head))
      println("*** Setting the start process parameter - by timemark timecode\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              startProcess = Some(StartProcessByTimeMark(Time(Timecode("12:13:14;15"))))))) \ "startProcess").head))
      println("*** Setting the start job parameter - by timemark normal play time\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              startProcess = Some(StartProcessByTimeMark(Time(NormalPlayTime(1000l))))))) \ "startProcess").head))
      println("*** Setting the start process parameter - by edit unit\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              startProcess = Some(StartProcessByTimeMark(Time(EditUnitNumber(3598, 60, 1000, 1001))))))) \ "startProcess").head))
      println("*** Setting the start process parameter - by service defined\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              startProcess = Some(StartProcessByServiceDefinedTime())))) \ "startProcess").head))
    }
    
    "provide examples of stop process kinds" in {
      val exampleJob = captureJob
      println("*** Setting the stop process parameter - by duration normal play time\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByTime(System.currentTimeMillis))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by duration timecode\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByDuration(Duration(Timecode("01:02:03;04"))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by duration normal play time\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByDuration(Duration(NormalPlayTime(3661000l))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by duration edit unit number\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByDuration(Duration(EditUnitNumber(7196, 60, 1000, 1001))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by timemark timecode\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByTimeMark(Time(Timecode("13:14:15;16"))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by timemark normal play time\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByDuration(Duration(EditUnitNumber(7196, 60, 1000, 1001))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by timemark edit unit number\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByTimeMark(Time(EditUnitNumber(14392, 60, 1000, 1001))))))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by service defined\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByServiceDefinedTime())))) \ "stopProcess").head))
      println("*** Setting the stop process parameter - by open end\n" + 
          printer.format((FimsXML.write(exampleJob.copy(serviceParameters = exampleJob.serviceParameters.copy(
              stopProcess = Some(StopProcessByOpenEnd())))) \ "stopProcess").head))
    } 
    
    "allow a single job to be queried" ignore {
      postCaptureJob(captureJob)
      val response = api.path("job/" + FimsString.write(captureJob.resourceID)).header("X-FIMS-Version", "1.1")
        .accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val testJobResponse = response.getEntity(classOf[CaptureJobType])
      testJobResponse should not be (null)
      println("*** Querying/polling one capture job - response\n" + printer.format(FimsXML.write(testJobResponse)))
      testJobResponse.resourceID should equal (captureJob.resourceID)
      testJobResponse.revisionID should equal (Some("1"))
      testJobResponse.location should equal (Some(new java.net.URI("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")))
      testJobResponse.resourceCreationDate.isDefined should be (true)
      testJobResponse.resourceModifiedDate.isDefined should be (true)
      testJobResponse.extensionGroup.isDefined should be (false)
    }
    
    "allow the listing of a number of jobs with default detail" ignore {
      postCaptureJob(captureJob)
      val response = api.path("job").accept("application/xml").header("X-FIMS-Version", "1.1").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val jobsXML = XML.load(response.getEntityInputStream())
      println("*** Listing capture jobs - default response\n" + printer.format(jobsXML))
      jobsXML.head.label should be ("jobs")
      (jobsXML \ "@detail").text should be ("full")
    } 
    
    "allow the listing of a number of jobs with full detail" ignore {
      postCaptureJob(captureJob)
      val response = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "full"); m})
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val jobsXML = XML.load(response.getEntityInputStream())
      println("4.4.2 Listing capture jobs - full response\n" + printer.format(jobsXML))
      jobsXML.head.label should be ("jobs")
      (jobsXML \ "@detail").text should be ("full")
    } 

    "allow the listing of a number of jobs with summary detail" ignore {
      postCaptureJob(captureJob)
      val response = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "summary"); m})
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val jobsXML = XML.load(response.getEntityInputStream())
      println("*** Listing capture jobs - summary response\n" + printer.format(jobsXML))
      jobsXML.head.label should be ("jobs")
      (jobsXML \ "@detail").text should be ("summary")
    } 

    "allow the listing of a number of jobs with link detail" ignore {
      postCaptureJob(captureJob)
      val response = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "link"); m})
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val jobsXML = XML.load(response.getEntityInputStream())
      println("*** Listing capture jobs - link response\n" + printer.format(jobsXML))
      jobsXML.head.label should be ("jobs")
      (jobsXML \ "@detail").text should be ("link")
    }
            
    "listing of jobs with skip and limit constraints" ignore {
      val jobRequests = for ( jobNumber <- 1 to 20 ) yield CaptureJob(
            captureJob.resourceParameters.copy(UUID.randomUUID),
            captureJob.baseParameters,
            captureJob.serviceParameters.copy(sourceID = "rtp://224.0.0." + jobNumber + ":5000"))
      postContentPlaceholder(contentPlaceholder)
      val jobResponses = jobRequests.map(x => api.path("job").accept("application/xml").header("X-FIMS-Version", "1.1")
          .header("Content-Type", "application/xml").post(classOf[ClientResponse], printer.format(FimsXML.write(x))))
      val response = api.path("job").queryParams({val m = new MultivaluedMapImpl(); m.add("detail", "summary"); m.add("skip", "10"); m.add("limit", "6"); m})
      	.header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      response.getStatus should be (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val jobsXML = XML.load(response.getEntityInputStream())
      println("*** List capture jobs - skip and limit\n" + printer.format(jobsXML))
      (jobsXML \\ "job").size should equal (6)
      (jobsXML \ "@pages").text should equal ("4")
      (jobsXML \ "@page").text should equal ("2") 
      (jobsXML \ "@totalSize").text should equal ("20")
      (jobsXML \ "@detail").text should equal ("summary")
    }
    
    "allow the creation of a job with aysnchronous endpoints" ignore {
      val captureJobWithNotify = captureJob.copy(resourceParameters = captureJob.resourceParameters.copy(notifyAt =
        AsyncEndpoint("http://localhost:9001/api/postecho/reply", "http://localhost:9001/api/postecho/fault")))
      println("*** Change notifications - request\n" + printer.format(FimsXML.write(captureJobWithNotify)))
      val response = postCaptureJob(captureJobWithNotify)
      response.getStatus should equal (201)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val testJobResponse = response.getEntity(classOf[CaptureJobType])
      testJobResponse should not be (null)
      println("*** Change notification - response\n" + printer.format(FimsXML.write(testJobResponse)))
      testJobResponse.resourceID should equal (captureJob.resourceID)
      testJobResponse.revisionID should equal (Some("1"))
      testJobResponse.location should equal (Some(new java.net.URI("http://localhost:9000/api/job/urn:uuid:c8bd1a79-53ed-4c0a-8bcd-fc3a806c4600")))
      testJobResponse.resourceCreationDate.isDefined should be (true)
      testJobResponse.resourceModifiedDate.isDefined should be (true)
      testJobResponse.extensionGroup.isDefined should be (false)
      testJobResponse.notifyAt.isDefined should be (true)
      testJobResponse.notifyAt.get.replyTo should equal (new java.net.URI("http://localhost:9001/api/postecho/reply"))
      testJobResponse.notifyAt.get.faultTo should equal (new java.net.URI("http://localhost:9001/api/postecho/fault"))
    } 
    
    "allow the update of the start process time for a capture job" ignore {
      val updatedDetails = StartProcessByTime(System.currentTimeMillis + 40000l)
      val updatedDetailsAsML = printer.format(XMLNamespaceProcessor.setNameSpaceIfAbsent(
          <startProcess xsi:type="bms:StartProcessByTimeType">{FimsXML.write(Some(updatedDetails))}</startProcess>, Namespaces.cmsDefault).head)
      println("*** Changing the start process time - request\n" + updatedDetailsAsML)
      val baseJob = captureJob
      postCaptureJob(baseJob)
      
      val response = api.path("job/" + FimsString.write(baseJob.resourceID) + "/startProcess").accept("application/xml")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").put(classOf[ClientResponse], updatedDetailsAsML)
      // println(response.getEntity(classOf[String]))  
      response.getStatus should equal (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      val updatedJobResponse = response.getEntity(classOf[CaptureJobType])
      updatedJobResponse should not be (null)
      println("*** Changing the start process time - response\n" + printer.format(FimsXML.write(updatedJobResponse)))
      updatedJobResponse.startProcess should equal (Some(updatedDetails))
      updatedJobResponse.revisionID should equal (Some("2"))
    } 
    
    "allow the update of the stop process time for a capture job" ignore {
      val updatedDetails = StopProcessByDuration(Duration(NormalPlayTime(82000l)))
      val updatedDetailsAsXML = printer.format(XMLNamespaceProcessor.setNameSpaceIfAbsent(
          <stopProcess xsi:type="bms:StopProcessByDurationType">{FimsXML.write(Some(updatedDetails))}</stopProcess>, Namespaces.cmsDefault).head)
      println("*** Changing the stop process time - request\n" + updatedDetailsAsXML)
      val baseJob = captureJob
      postCaptureJob(baseJob)
      val response = api.path("job/" + FimsString.write(baseJob.resourceID) + "/stopProcess").accept("application/xml")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").put(classOf[ClientResponse], updatedDetailsAsXML)
      response.getStatus should equal (200)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      val updatedJobResponse = response.getEntity(classOf[CaptureJobType])
      updatedJobResponse should not be (null)
      println("*** Changing the stop process time - response\n" + printer.format(FimsXML.write(updatedJobResponse)))
      updatedJobResponse.stopProcess should equal (Some(updatedDetails))
      updatedJobResponse.revisionID should equal (Some("2"))
    }

    "allow a queued or running job to be canceled" ignore {
      val baseJob = captureJob
      val jobCommand = ManageJobRequest(baseJob.resourceID, Cancel)
      val jobCommandAsXML = printer.format(FimsXML.write(jobCommand))
      println("*** Canceling a capture job - request\n" + jobCommandAsXML)
      
      postCaptureJob(captureJob)
      val response = api.path("job/" + FimsString.write(baseJob.resourceID) + "/manage")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobCommandAsXML)
      response.getStatus should be (202)
      response.getLength should be <= (0)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      Thread.sleep(500)
      val getResponse = api.path("job/" + FimsString.write(baseJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse.getStatus should be (200)
      getResponse.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      val canceledJob = getResponse.getEntity(classOf[CaptureJobType])
      canceledJob should not be (null)
      canceledJob.status should equal (Some(JobStatusType.Canceled))
    } 
    
    "allow a stoppable job to be stopped in time" ignore {
      val baseJob = captureJob
      val stoppableJob = captureJob.copy(
          baseParameters = baseJob.baseParameters.copy(
              startJob = StartJobByTime(System.currentTimeMillis() + 1000l)),
          serviceParameters = baseJob.serviceParameters.copy(
            stopProcess = StopProcessByOpenEnd()))
      val jobCommand = ManageJobRequest(stoppableJob.resourceID, Stop)
      val jobCommandAsXML = printer.format(FimsXML.write(jobCommand))
      println("*** Stopping a capture job - request\n" + jobCommandAsXML)
          
      postCaptureJob(stoppableJob)
      Thread.sleep(2000l) // Wait for job to start up
           
      val response = api.path("job/" + FimsString.write(stoppableJob.resourceID) + "/manage")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobCommandAsXML)
      response.getStatus should be (202)
      response.getLength should be <= (0)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")

      Thread.sleep(500) // Wait for stop
      val getResponse = api.path("job/" + FimsString.write(stoppableJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse.getStatus should be (200)
      getResponse.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      val stoppedJob = getResponse.getEntity(classOf[CaptureJobType])
      stoppedJob should not be (null)
      stoppedJob.status should equal (Some(JobStatusType.Stopped))
    }
    
    "delete a job in a finished state" ignore {
      val baseJob = captureJob
      val jobCommand = ManageJobRequest(baseJob.resourceID, Cancel)
      val jobCommandAsXML = printer.format(FimsXML.write(jobCommand))
 
      postCaptureJob(baseJob)
      val cancelResponse = api.path("job/" + FimsString.write(baseJob.resourceID) + "/manage")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobCommandAsXML)
      cancelResponse.getStatus should be (202)
      cancelResponse.getLength should be <= (0)
      cancelResponse.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      Thread.sleep(500)
      val getResponse1 = api.path("job/" + FimsString.write(captureJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse1.getStatus should be (200)
      getResponse1.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      val canceledJob = getResponse1.getEntity(classOf[CaptureJobType])
      canceledJob should not be (null)
      canceledJob.status should equal (Some(JobStatusType.Canceled))
      
      val stopResponse = api.path("job/" + FimsString.write(captureJob.resourceID))
        .header("X-FIMS-Version", "1.1").delete(classOf[ClientResponse])
      stopResponse.getStatus should be (204)
      stopResponse.getLength should be <= (0)     
      val getResponse2 = api.path("job/" + FimsString.write(captureJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse2.getStatus should be (404)
    }

    "purge jobs in a finished state" ignore {
      val baseJob = captureJob
      val jobCommand = ManageJobRequest(baseJob.resourceID, Cancel)
      val jobCommandAsXML = printer.format(FimsXML.write(jobCommand))
      println("*** Canceling a capture job - request\n" + jobCommandAsXML)
      
      postCaptureJob(captureJob)
      val response = api.path("job/" + FimsString.write(baseJob.resourceID) + "/manage")
        .header("X-FIMS-Version", "1.1").header("Content-Type", "application/xml").post(classOf[ClientResponse], jobCommandAsXML)
      response.getStatus should be (202)
      response.getLength should be <= (0)
      response.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      
      Thread.sleep(500)
      val getResponse = api.path("job/" + FimsString.write(baseJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse.getStatus should be (200)
      getResponse.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      val canceledJob = getResponse.getEntity(classOf[CaptureJobType])
      canceledJob should not be (null)
      canceledJob.status should equal (Some(JobStatusType.Canceled))
      
      val purgeResponse = api.path("job/purge").post(classOf[ClientResponse])
      purgeResponse.getStatus should be (204)
      purgeResponse.getLength should be <= (0)      
      purgeResponse.getHeaders.getFirst("X-FIMS-Version") should equal ("1.1")
      val getResponse2 = api.path("job/" + FimsString.write(captureJob.resourceID))
        .header("X-FIMS-Version", "1.1").accept("application/xml").get(classOf[ClientResponse])
      getResponse2.getStatus should be (404)
    } 
  }

  "The FIMS REST API when running in CXF" should {
    "generate a WADL description of the service" ignore {
      if (fimsRestInstance.isInstanceOf[FimsRestCXFInstance]) {
	    val wadlResponse = api.path("").queryParams({val m = new MultivaluedMapImpl(); m.add("_wadl", ""); m}).get(classOf[ClientResponse])
	    wadlResponse.getStatus should be (200)
	    val wadlXML = XML.load(wadlResponse.getEntityInputStream)
	    println("*** RESTful API - wadl\n" + printer.format(wadlXML))
      }
    }
  }
}