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

package tv.amwa.ebu.fims.rest.model
import akka.util.ByteString
import java.util.UUID
import tv.amwa.ebu.fims.rest.converter.Converter

sealed trait ResourceID {
  import tv.amwa.ebu.fims.rest.converter.StringConverters.ResourceIDConverter
  def bytes: ByteString
  override def toString = implicitly[Converter[ResourceID, String]].write(this)
}

object ResourceID {
  import tv.amwa.ebu.fims.rest.converter.StringConverters.ResourceIDConverter
  implicit def stringToResourceID(value: String): ResourceID =
    implicitly[Converter[ResourceID, String]].read(value)
  implicit def resourceIDToString(id: ResourceID): String = 
    implicitly[Converter[ResourceID, String]].write(id)
  implicit def uuidToResourceID(uuid: UUID): ResourceID = UUIDBasedResourceID(uuid)
  implicit def resourceIDToUUID(id: ResourceID): UUID = id match { 
    case uuid : UUIDBasedResourceID => uuid.uuid
    case _ => throw new IllegalArgumentException("ResourceID is not of UUID type.")
  }
}

case class UMIDBasedResourceID(idBytes: ByteString) extends ResourceID {
  require(idBytes.length == 32, "A byte string representation of a UMID must contain 32 bytes. " + bytes.length + " were present.")
  override def bytes = idBytes
} 

case class ULBasedResourceID(idBytes: ByteString) extends ResourceID {
  require(idBytes.length == 16, "A byte string representattion of a SMPTE Universal Label must contain 16 bytes. " + bytes.length + " were present.")
  override def bytes = idBytes
}

case class UUIDBasedResourceID(uuid: UUID) extends ResourceID {
  private[this] def longToByteString(value: Long) =
    ByteString((for (x <- 56.to(0, -8)) yield (((value >>> x) & 0xff).toByte)): _*)
  def bytes = longToByteString(uuid.getMostSignificantBits()) ++ longToByteString(uuid.getLeastSignificantBits())
}

object UUIDBasedResourceID {
  val fixedStartingBytes = ByteString(0x06, 0x0a, 0x2b, 0x34, 0x01, 0x01, 0x01, 0x05, 0x01, 0x01, 0x0f, 0x20, 0x13, 0x00, 0x00, 0x00)
  implicit def uuidBasedToUmidBased(id: UUIDBasedResourceID): UMIDBasedResourceID =
    UMIDBasedResourceID(fixedStartingBytes ++ id.bytes)
}

case class EmptyResourceID() extends ResourceID {
  def bytes = ByteString()
}
