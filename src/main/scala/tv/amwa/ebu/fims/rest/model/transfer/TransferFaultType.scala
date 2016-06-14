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

package tv.amwa.ebu.fims.rest.model.transfer

import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import tv.amwa.ebu.fims.rest.model.InnerFault
import tv.amwa.ebu.fims.rest.model.ErrorCodeDetails
import tv.amwa.ebu.fims.rest.model.MakeBaseErrors
import tv.amwa.ebu.fims.rest.model.FimsException
import tv.amwa.ebu.fims.rest.model.FimsException

trait TransferFaultType extends FaultType {
    val extendedCode: Option[TransferErrorCodeType]
}

case class TrnasferFault(
    code: ErrorCodeType,
    description: Option[String] = None,
    detail: Option[String] = None,
    innerFault: Seq[InnerFault] = Nil,
    extendedCode: Option[TransferErrorCodeType] = None) extends TransferFaultType
    
sealed trait TransferErrorCodeType extends ErrorCodeDetails

object TransferErrorCodeType {
  def fromString(value: String): TransferErrorCodeType = value match {
    case "DAT_S01_0001" => DAT_S01_0001
    case "DAT_S01_0002" => DAT_S01_0002
    case "DAT_S01_0003" => DAT_S01_0003
    case "SVC_S01_0001" => SVC_S01_0001
    case "SVC_S01_0002" => SVC_S01_0002
    case "SVC_S01_0003" => SVC_S01_0003
    case "SVC_S01_0004" => SVC_S01_0004
    case "SVC_S01_0005" => SVC_S01_0005
    case "SVC_S01_0006" => SVC_S01_0006
    case "SVC_S01_0007" => SVC_S01_0007
    case "SVC_S01_0008" => SVC_S01_0008
    case "SVC_S01_0009" => SVC_S01_0009
    case "SVC_S01_0010" => SVC_S01_0010
    case "INF_S01_0001" => INF_S01_0001
    case "INF_S01_0002" => INF_S01_0002
  }
  val allCodes: List[TransferErrorCodeType] = List(
      DAT_S01_0001, DAT_S01_0002, DAT_S01_0003, SVC_S01_0001,
      SVC_S01_0002, SVC_S01_0003, SVC_S01_0004, SVC_S01_0005,
      SVC_S01_0006, SVC_S01_0007, SVC_S01_0008, SVC_S01_0009,
      SVC_S01_0010, INF_S01_0001, INF_S01_0002)
  case object DAT_S01_0001 extends TransferErrorCodeType {
    override val description = "Invalid URI protocol specified for transport operations."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "DAT_S01_0001" } 
  case object DAT_S01_0002 extends TransferErrorCodeType {
    override val description = "Invalid output directory or target URI path."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "DAT_S01_0002" } 
  case object DAT_S01_0003 extends TransferErrorCodeType {
    override val description = "Incorrect hash. File received does not have same hash as specified in the file hash value property."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "DAT_S01_0003" } 
  case object SVC_S01_0001 extends TransferErrorCodeType {
    override val description = "Unsupported protocol."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S01_0001" } 
  case object SVC_S01_0002 extends TransferErrorCodeType {
    override val description = "Unsupported hash type."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S01_0002" } 
  case object SVC_S01_0003 extends TransferErrorCodeType {
    override val description = "Encryption not supported."
    override val statusCode = Some(501)
    override val statusDescription = Some("Not implemented.")
    override def toString = "SVC_S01_0003" } 
  case object SVC_S01_0004 extends TransferErrorCodeType {
    override val description = "Authentication not supported."
    override val statusCode = Some(501)
    override val statusDescription = Some("Not implemented.")
    override def toString = "SVC_S01_0004" }
  case object SVC_S01_0005 extends TransferErrorCodeType {
    override val description = "Integrity check not supported."
    override val statusCode = Some(501)
    override val statusDescription = Some("Not implemented.")
    override def toString = "SVC_S01_0005" } 
  case object SVC_S01_0006 extends TransferErrorCodeType {
    override val description = "File too large."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "SVC_S01_0006" } 
  case object SVC_S01_0007 extends TransferErrorCodeType {
    override val description = "Times not possible."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S01_0007" } 
  case object SVC_S01_0008 extends TransferErrorCodeType {
    override val description = "Incorrect file size."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override def toString = "SVC_S01_0008" } 
  case object SVC_S01_0009 extends TransferErrorCodeType {
    override val description = "Rejected by operator."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override def toString = "SVC_S01_0009" } 
  case object SVC_S01_0010 extends TransferErrorCodeType {
    override val description = "Transfer process ended unexpectedly."
    override val notes = Some("No status code as used in a fault notification only.")
    override def toString = "SVC_S01_0010" } 
  case object INF_S01_0001 extends TransferErrorCodeType {
    override val description = "Network link with insufficient bandwidth."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "INF_S01_0001" } 
  case object INF_S01_0002 extends TransferErrorCodeType {
    override val description = "Link timed out."
    override val statusCode = Some(504)
    override val statusDescription = Some("Gatway timeout.")
    override def toString = "INF_S01_0002" } 
}

class InvalidURIProtocol extends FimsException { val errorCode = TransferErrorCodeType.DAT_S01_0001 }
class InvalidOutputDirectory extends FimsException { val errorCode = TransferErrorCodeType.DAT_S01_0002 }
class IncorrectHash extends FimsException { val errorCode = TransferErrorCodeType.DAT_S01_0003 }
class UnsupportedProtocol extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0001 }
class UnsupportedHashType extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0002 }
class EncryptionNotSupported extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0003 }
class AuthenticationNotSupported extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0004 }
class IntegrityCheckNotSupported extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0005 }
class FileTooLarge extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0006 }
class TimesNotPossible extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0007 }
class IncorrectFileSize extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0008 }
class RejectedByOperator extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0009 }
class TransferProcessEnded extends FimsException { val errorCode = TransferErrorCodeType.SVC_S01_0010 }
class InsufficientBandwidth extends FimsException { val errorCode = TransferErrorCodeType.INF_S01_0001 }
class LinkTimedOut extends FimsException { val errorCode = TransferErrorCodeType.INF_S01_0002 } 

object TransformErrorSchemaGen extends App {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  def makeAllErrorCodes() = {
    <simpleType name="TransferErrorCodeType">
      <annotation>
	    <documentation source="urn:x-fims:description">Specific error codes for the
		  transfer :
		    - INF_S01_xxxx: Infrastructure errors (system, storage, network, memory, processor)
		    - DAT_S01_xxxx: Data errors (validation, missing, duplication)
			- SVC_S01_xxxx: Operation errors (existence, support, lock, connection, failure)
			- SEC_S01_xxxx: Security errors (authentication, authorization)
		  </documentation>
		  <documentation source="urn:x-fims:normativeRequirement"/>
		  <documentation source="urn:x-fims:serviceDescription"/>
		  <documentation source="urn:x-fims:contentOfServiceDescription"/>
		</annotation>
		<restriction base="string">{
	     TransferErrorCodeType.allCodes map {MakeBaseErrors.makeSingleEntry(_) } }
	  </restriction>
	</simpleType>
  }
  println(printer.format(makeAllErrorCodes()))
}

