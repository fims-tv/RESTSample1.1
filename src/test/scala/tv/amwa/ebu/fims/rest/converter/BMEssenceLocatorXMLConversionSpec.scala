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
import tv.amwa.ebu.fims.rest.model.BMEssenceLocatorType
import tv.amwa.ebu.fims.rest.model.SimpleFileLocator
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.Storage
import tv.amwa.ebu.fims.rest.model.ContainerMimeType
import tv.amwa.ebu.fims.rest.model.ListFileLocator
import tv.amwa.ebu.fims.rest.model.FolderLocator
import tv.amwa.ebu.fims.rest.model.StorageTypes


@RunWith(classOf[JUnitRunner])
class BMEssenceLocatorXMLConversionSpec extends WordSpec with Matchers {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  
  "A manually created simple file locator" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
       val locator = BMEssenceLocatorXMLConversionSpec.simpleFileLocator
       
       val outNodes = FimsXML.write(locator)
       println(printer.format(outNodes))
       val inValue = FimsXML.read[BMEssenceLocatorType](outNodes)
       println(locator.toString)
       println(inValue.toString + "\n")
      
       XMLRoundTrip.writewriteNodeThenRead[BMEssenceLocatorType](locator) should equal (locator)
       XMLRoundTrip.writeToBytesThenRead[BMEssenceLocatorType](locator) should equal (locator)
    }
    
    "be validated against the FIMS schema" in {
      val locator = BMEssenceLocatorXMLConversionSpec.simpleFileLocator
      
      val outNodes = FimsXML.write(locator)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }

  "A manually created list file locator" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
       val locator = BMEssenceLocatorXMLConversionSpec.listFileLocator
       
       val outNodes = FimsXML.write(locator)
       println(printer.format(outNodes))
       val inValue = FimsXML.read[BMEssenceLocatorType](outNodes)
       println(locator.toString)
       println(inValue.toString + "\n")
      
       XMLRoundTrip.writewriteNodeThenRead[BMEssenceLocatorType](locator) should equal (locator)
       XMLRoundTrip.writeToBytesThenRead[BMEssenceLocatorType](locator) should equal (locator)
    }

    "be validated against the FIMS schema" in {
      val locator = BMEssenceLocatorXMLConversionSpec.listFileLocator
      
      val outNodes = FimsXML.write(locator)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }

  "A manually created folder locator" should {
    
    "be serialized to XML and de-serialized back to a case class" in {
       val locator = BMEssenceLocatorXMLConversionSpec.folderLocator
       
       val outNodes = FimsXML.write(locator)
       println(printer.format(outNodes))
       val inValue = FimsXML.read[BMEssenceLocatorType](outNodes)
       println(locator.toString)
       println(inValue.toString + "\n")
      
       XMLRoundTrip.writewriteNodeThenRead[BMEssenceLocatorType](locator) should equal (locator)
       XMLRoundTrip.writeToBytesThenRead[BMEssenceLocatorType](locator) should equal (locator)
    }

    "be validated against the FIMS schema" in {
      val locator = BMEssenceLocatorXMLConversionSpec.folderLocator
      
      val outNodes = FimsXML.write(locator)
      XMLValidator.validate(printer.format(outNodes), "baseMediaService-V1_1_0.xsd") should be (true)
    }
  }
}

object BMEssenceLocatorXMLConversionSpec {
  val simpleFileLocator = SimpleFileLocator(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Storage(StorageTypes.Online, "one", "two", "http://3.com"),
      "Biscuit tin",
      ContainerMimeType("video/mpeg", "one", "two", "http://3.com"),
      None,
      new java.net.URI("file:///tmp/test.mpg"))
  val listFileLocator =  ListFileLocator(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Storage(StorageTypes.Online, "one", "two", "http://3.com"),
      "Biscuit tin",
      ContainerMimeType("video/mpeg", "one", "two", "http://3.com"),
      None,
      List("file:///tmp/test.mpg", "file:///tmp/test.mxf"))
  val folderLocator = FolderLocator(
      ResourceParameters(UUIDBasedResourceID(UUID.randomUUID)),
      Storage(StorageTypes.Online, "one", "two", "http://3.com"),
      "Biscuit tin",
      ContainerMimeType("application/mxf", "one", "two", "http://3.com"),
      None,
      new java.net.URI("file:///tmp/folder/"))
}