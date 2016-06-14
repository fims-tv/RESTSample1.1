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
import tv.amwa.ebu.fims.rest.model.ResourceID
import java.util.UUID
import tv.amwa.ebu.fims.rest.model.UMIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.ULBasedResourceID
import tv.amwa.ebu.fims.rest.model.EmptyResourceID
import tv.amwa.ebu.fims.rest.model.UUIDBasedResourceID
import tv.amwa.ebu.fims.rest.model.ULBasedResourceID
import tv.amwa.ebu.fims.rest.model.EmptyResourceID

object StringConverters {

  implicit object UUIDBasedResourceIDConverter extends Converter[UUIDBasedResourceID, String] {
    val URN_UUID_PREFIX = "urn:uuid:"
    def write(id: UUIDBasedResourceID) = {
      URN_UUID_PREFIX + id.uuid.toString
    }
    def read(value: String): UUIDBasedResourceID = {
      UUIDBasedResourceID(UUID.fromString(value.toLowerCase.trim.stripPrefix(URN_UUID_PREFIX)))
    }
  }
  
  implicit object UMIDBasedResourceIDConverter extends Converter[UMIDBasedResourceID, String] {
    val URN_UMID_PREFIX = "urn:smpte:umid:"
    def write(id: UMIDBasedResourceID) = {
      URN_UMID_PREFIX + id.bytes.grouped(4).map{x => HexBinary.toHex(x)}.mkString(".")
    }
    def read(value: String) = {
      UMIDBasedResourceID(HexBinary.fromHex(value.toLowerCase.trim.stripPrefix(URN_UMID_PREFIX).filter(_ != '.')))
    }
  }
  
  implicit object ULBasedResourceIDConverter extends Converter[ULBasedResourceID, String] {
    val URN_UL_PREFIX = "urn:smpte:ul:"
    def write(id: ULBasedResourceID) = {
      URN_UL_PREFIX + id.bytes.grouped(4).map{x => HexBinary.toHex(x)}.mkString(".")
    }
    def read(value: String) = {
      ULBasedResourceID(HexBinary.fromHex(value.toLowerCase.trim.stripPrefix(URN_UL_PREFIX).filter(_ != '.')))
    }
  }
  
  implicit object EmptyResourceIDConverter extends Converter[EmptyResourceID, String] {
    def write(id: EmptyResourceID) = ""
    def read(id: String) = EmptyResourceID()
  }

  implicit object ResourceIDConverter extends Converter[ResourceID, String] {
    val uuidRegex = """(urn:uuid:)?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"""
    val umidRegex = """(urn:smpte:umid:)?([0-9a-fA-F]{8}\.){7}[0-9a-fA-F]{8}"""
    val ulRegex = """(urn:smpte:ul:)?([0-9a-fA-F]{8}\.){3}[0-9a-fA-F]{8}"""
    def write(id: ResourceID) = {
      id match {
        case uuid @ UUIDBasedResourceID(_) => UUIDBasedResourceIDConverter.write(uuid)
        case umid @ UMIDBasedResourceID(_) => UMIDBasedResourceIDConverter.write(umid)
        case ul @ ULBasedResourceID(_) => ULBasedResourceIDConverter.write(ul)
        case empty @ EmptyResourceID() => EmptyResourceIDConverter.write(empty)
      }
    }
    def read(value: String): ResourceID = {
      value.toLowerCase.trim match {
        case uuid if uuid.matches(uuidRegex) => UUIDBasedResourceIDConverter.read(uuid)
        case umid if umid.matches(umidRegex) => UMIDBasedResourceIDConverter.read(umid)
        case ul if ul.matches(ulRegex) => ULBasedResourceIDConverter.read(ul)
        case empty if empty.length == 0 => EmptyResourceIDConverter.read(empty)
        case _ => throw new IllegalArgumentException("Given resource identifier does not match the FIMS specification.")
      }
    }
  }
}

object FimsString {
  def read[A](s: String)(implicit conv: Reader[A,String]): A = conv.read(s)
  def write[A](value: A)(implicit conv: Writer[A,String]): String = conv.write(value)
}