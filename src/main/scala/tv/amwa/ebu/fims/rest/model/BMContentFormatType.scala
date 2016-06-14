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

trait BMContentFormatType extends ResourceType {
    val bmEssenceLocators: Seq[BMEssenceLocatorType]
    val formatCollection: Option[FormatCollection]
    val duration: Option[Duration]
    val hash: Option[Hash]
    val packageSize: Option[Long]
    val mimeType: Option[MimeType]
  require(packageSize.map{_ >= 0}.getOrElse(true))
}

case class BMContentFormat(
    resourceParameters: ResourceParameters,
    bmEssenceLocators: Seq[BMEssenceLocatorType] = Nil,
    formatCollection: Option[FormatCollection] = None,
    duration: Option[Duration] = None,
    hash: Option[Hash] = None,
    packageSize: Option[Long] = None,
    mimeType: Option[MimeType] = None) extends BMContentFormatType
    
case class FormatCollection(
    videoFormat: Option[VideoFormatType] = None,
    audioFormat: Option[AudioFormatType] = None,
    dataFormat: Option[DataFormatType] = None,
    containerFormat: Option[ContainerFormatType] = None)
    
/** Provides information on the algorithm used in an integrity check process. 
 *  It is based on the 'hash' type defined in SMPTE ST 2032.
*/
case class Hash(
    hashFunction: HashFunctionTypes,
    value: ByteString)
    
sealed trait HashFunctionTypes

object HashFunctionTypes {
  def fromString(value: String): HashFunctionTypes = value match {
    case "CRC32" => CRC32
    case "CRC64" => CRC64
    case "MD5" => MD5
    case "SHA1" => SHA1
    case "SHA256" => SHA256
    case "SHA384" => SHA384
    case "SHA512" => SHA512
  }
}

case object CRC32 extends HashFunctionTypes { override def toString = "CRC32" }
case object CRC64 extends HashFunctionTypes { override def toString = "CRC64" }
case object MD5 extends HashFunctionTypes { override def toString = "MD5" }
case object SHA1 extends HashFunctionTypes { override def toString = "SHA1" }
case object SHA256 extends HashFunctionTypes { override def toString = "SHA256" }
case object SHA384 extends HashFunctionTypes { override def toString = "SHA384" }
case object SHA512 extends HashFunctionTypes { override def toString = "SHA512" }

case class MimeType(
    value: String,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None) extends TypeGroup[MimeType] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    this.copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
}
