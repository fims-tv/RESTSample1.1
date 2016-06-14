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
import tv.amwa.ebu.fims.rest.commons.Loggable
import java.util.UUID
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.converter.StringConverters._
import akka.util.ByteString
import tv.amwa.ebu.fims.rest.model.UMIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.ULBasedResourceID
import tv.amwa.ebu.fims.rest.model.ResourceID

@RunWith(classOf[JUnitRunner])
class ResourceIDConversionSpec extends WordSpec with Matchers with Loggable  {
  "A UUID based ResourceID" should {
    "be converted to/from a URN representation" in {
      val uuid = UUID.randomUUID
      val id = UUIDBasedResourceID(uuid)
      val serialized = FimsString.write[ResourceID](id)
      val deserialized = FimsString.read[UUIDBasedResourceID](serialized)
      deserialized should equal(id)
    }
    
    "be converted from a sample URN" in {
      val idString = "urn:uuid:be992bc2-44f4-448d-aeae-53a4b4e3e6f5"
      val deserialized = FimsString.read[UUIDBasedResourceID](idString)
      val uuid = UUID.fromString(idString.stripPrefix("urn:uuid:"))
      deserialized should equal(UUIDBasedResourceID(uuid))      
    }
    
    "be converted from a sample 36 character string" in {
      val idString = "be992bc2-44f4-448d-aeae-53a4b4e3e6f5"
      val deserialized = FimsString.read[UUIDBasedResourceID](idString)
      val uuid = UUID.fromString(idString)
      deserialized should equal(UUIDBasedResourceID(uuid))      
    }
  }
  
  "A UMID based ResourceID" should {
    "be converted to/from a UMID representation" in {
      val umidBytes = ByteString({ 
        val random = new scala.util.Random()
        val randomBytes = Array.fill[Byte](32)(0)
        random.nextBytes(randomBytes)
        randomBytes
      })
      val id = UMIDBasedResourceID(umidBytes)
      val serialized = FimsString.write[ResourceID](id)
      val deserialized = FimsString.read[UMIDBasedResourceID](serialized)
      deserialized should equal(id)
    }
    
    "be converted from a sample URN" in {
       val idString = "urn:smpte:umid:be992bc2.44f4448d.aeae53a4.b4e3e6f5.be992bc2.44f4448d.aeae53a4.b4e3e6f5"
       val deserialized = FimsString.read[UMIDBasedResourceID](idString)
       val umid = HexBinary.fromHex(idString.stripPrefix("urn:smpte:umid:").filter(_ != '.'))
       deserialized should equal(UMIDBasedResourceID(umid))      
    }
    
    "be converted from a sample 51 character string" in {
       val idString = "be992bc2.44f4448d.aeae53a4.b4e3e6f5.be992bc2.44f4448d.aeae53a4.b4e3e6f5"
       val deserialized = FimsString.read[UMIDBasedResourceID](idString)
       val umid = HexBinary.fromHex(idString.filter(_ != '.'))
       deserialized should equal(UMIDBasedResourceID(umid))      
    }
    
    "not be converted from a bad string" in {
      val badString = "I'm not a UMID"
      intercept[IllegalArgumentException] {
        FimsString.read[UMIDBasedResourceID](badString)
      }
    }
  }
  
  "A UL based ResourceID" should {
    "be converted to/from a UL representation" in {
      val umidBytes = ByteString({ 
        val random = new scala.util.Random()
        val randomBytes = Array.fill[Byte](16)(0)
        random.nextBytes(randomBytes)
        randomBytes
      })
      val id = ULBasedResourceID(umidBytes)
      val serialized = FimsString.write[ResourceID](id)
      val deserialized = FimsString.read[ULBasedResourceID](serialized)
      deserialized should equal(id)
    }
    
    "be converted from a sample URN" in {
       val idString = "urn:smpte:ul:be992bc2.44f4448d.aeae53a4.b4e3e6f5"
       val deserialized = FimsString.read[ULBasedResourceID](idString)
       val ul = HexBinary.fromHex(idString.stripPrefix("urn:smpte:ul:").filter(_ != '.'))
       deserialized should equal(ULBasedResourceID(ul))      
    }
    
    "be converted from a sample 51 character string" in {
       val idString = "be992bc2.44f4448d.aeae53a4.b4e3e6f5"
       val deserialized = FimsString.read[ULBasedResourceID](idString)
       val ul = HexBinary.fromHex(idString.filter(_ != '.'))
       deserialized should equal(ULBasedResourceID(ul))      
    }
    
    "not be converted from a bad string" in {
      val badString = "I'm not a UL"
      intercept[IllegalArgumentException] {
        FimsString.read[ULBasedResourceID](badString)
      }
    }

   "not be converted from a short UL" in {
      val badString = "urn:smpte:ul:be992bc2.44f4448d.aeae53a4.b4e3e6"
      intercept[IllegalArgumentException] {
        FimsString.read[ULBasedResourceID](badString)
      }
    }
  }

}