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

import scala.xml.Text
import scala.xml.NodeSeq

trait FaultType {
    val code: ErrorCodeType
    val description: Option[String]
    val detail: Option[String]
    val innerFault: Seq[InnerFault]
}

/** Details of a fault. This type can be extended by each service to provide additional error codes. 
*/
case class Fault(
    code: ErrorCodeType,
    description: Option[String] = None,
    detail: Option[String] = None,
    innerFault: Seq[InnerFault] = Nil) extends FaultType


/** Additional description of the lower-level errors. */
case class InnerFault(
    code: String,
    description: Option[String] = None,
    detail: Option[String] = None)

trait ErrorCodeDetails {
  val description: String
  val notes: Option[String] = None
  val statusCode: Option[Int] = None 
  val statusDescription: Option[String] = None
  val introducedInVersion: Option[String] = None
  val modifiedInVersion: Option[String] = None
}
    
sealed trait ErrorCodeType extends ErrorCodeDetails
    
object ErrorCodeType {
  def fromString(value: String): ErrorCodeType = value match {
    case "INF_S00_0001" => INF_S00_0001
    case "INF_S00_0002" => INF_S00_0002
    case "INF_S00_0003" => INF_S00_0003
    case "INF_S00_0004" => INF_S00_0004
    case "INF_S00_0005" => INF_S00_0005
    case "INF_S00_0006" => INF_S00_0006
    case "INF_S00_0007" => INF_S00_0007
    case "SVC_S00_0001" => SVC_S00_0001
    case "SVC_S00_0002" => SVC_S00_0002
    case "SVC_S00_0003" => SVC_S00_0003
    case "SVC_S00_0004" => SVC_S00_0004
    case "SVC_S00_0005" => SVC_S00_0005
    case "SVC_S00_0006" => SVC_S00_0006
    case "SVC_S00_0007" => SVC_S00_0007
    case "SVC_S00_0008" => SVC_S00_0008
    case "SVC_S00_0009" => SVC_S00_0009
    case "SVC_S00_0010" => SVC_S00_0010
    case "SVC_S00_0011" => SVC_S00_0011
    case "SVC_S00_0012" => SVC_S00_0012
    case "SVC_S00_0013" => SVC_S00_0013
    case "SVC_S00_0014" => SVC_S00_0014
    case "SVC_S00_0015" => SVC_S00_0015
    case "SVC_S00_0016" => SVC_S00_0016
    case "SVC_S00_0017" => SVC_S00_0017
    case "SVC_S00_0018" => SVC_S00_0018
    case "SVC_S00_0019" => SVC_S00_0019
    case "SVC_S00_0020" => SVC_S00_0020
    case "SVC_S00_0021" => SVC_S00_0021
    case "SVC_S00_0022" => SVC_S00_0022
    case "DAT_S00_0001" => DAT_S00_0001
    case "DAT_S00_0002" => DAT_S00_0002
    case "DAT_S00_0003" => DAT_S00_0003
    case "DAT_S00_0004" => DAT_S00_0004
    case "DAT_S00_0005" => DAT_S00_0005
    case "DAT_S00_0006" => DAT_S00_0006
    case "DAT_S00_0007" => DAT_S00_0007
    case "DAT_S00_0008" => DAT_S00_0008
    case "DAT_S00_0009" => DAT_S00_0009
    case "DAT_S00_0010" => DAT_S00_0010
    case "DAT_S00_0011" => DAT_S00_0011
    case "DAT_S00_0012" => DAT_S00_0012
    case "DAT_S00_0013" => DAT_S00_0013
    case "DAT_S00_0014" => DAT_S00_0014
    case "EXT_S00_0000" => EXT_S00_0000
    case "SEC_S00_0001" => SEC_S00_0001
    case "SEC_S00_0002" => SEC_S00_0002
    case "SEC_S00_0003" => SEC_S00_0003
    case "SEC_S00_0004" => SEC_S00_0004
    case "SEC_S00_0005" => SEC_S00_0005
    case "SEC_S00_0006" => SEC_S00_0006
    case _ => throw new IllegalArgumentException("Error status code '" + value + "' does not match a known base status code.")
  }
  val allCodes: List[ErrorCodeType] = List(
      INF_S00_0001, INF_S00_0002, INF_S00_0003, INF_S00_0004,
      INF_S00_0005, INF_S00_0006, INF_S00_0007, SVC_S00_0001,
      SVC_S00_0002, SVC_S00_0003, SVC_S00_0004, SVC_S00_0005,
      SVC_S00_0006, SVC_S00_0007, SVC_S00_0008, SVC_S00_0009,
      SVC_S00_0010, SVC_S00_0011, SVC_S00_0012, SVC_S00_0013,
      SVC_S00_0014, SVC_S00_0015, SVC_S00_0016, SVC_S00_0017,
      SVC_S00_0018, SVC_S00_0019, SVC_S00_0020, SVC_S00_0021,
      SVC_S00_0022, DAT_S00_0001, DAT_S00_0002, DAT_S00_0003,
      DAT_S00_0004, DAT_S00_0005, DAT_S00_0006, DAT_S00_0007,
      DAT_S00_0008, DAT_S00_0009, DAT_S00_0010, DAT_S00_0011,
   	  DAT_S00_0012, DAT_S00_0013, DAT_S00_0014, EXT_S00_0000,
      SEC_S00_0001, SEC_S00_0002, SEC_S00_0003, SEC_S00_0004,
      SEC_S00_0005, SEC_S00_0006)
  case object INF_S00_0001 extends ErrorCodeType { 
    override val description = "System unavailable."
    override val statusCode = Some(503)
    override val statusDescription = Some("Service unavailable.")
    override def toString = "INF_S00_0001" }
  case object INF_S00_0002 extends ErrorCodeType { 
    override val description = "System timeout."
    override val statusCode = Some(408)
    override val statusDescription = Some("Request timeout.")
    override def toString = "INF_S00_0002" }
  case object INF_S00_0003 extends ErrorCodeType { 
    override val description = "System internal error."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "INF_S00_0003" }
  case object INF_S00_0004 extends ErrorCodeType { 
    override val description = "Unable to connect to the database."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "INF_S00_0004" }
  case object INF_S00_0005 extends ErrorCodeType { 
    override val description = "System out of memory."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "INF_S00_0005" }
  case object INF_S00_0006 extends ErrorCodeType { 
    override val description = "System out of disk space."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "INF_S00_0006" }
  case object INF_S00_0007 extends ErrorCodeType {
    override val description = "Maximum number of connections reached."
    override val statusCode = Some(503)
    override val statusDescription = Some("Service unavailable.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "INF_S00_0007" }
  case object SVC_S00_0001 extends ErrorCodeType { 
    override val description = "Job command is not currently supported by the service URI specified."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Include explanation.")
    override def toString = "SVC_S00_0001" }
  case object SVC_S00_0002 extends ErrorCodeType { 
    override val description = "Queue command is not currently supported by the service or the device."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Include explanation.")
    override def toString = "SVC_S00_0002" }
  case object SVC_S00_0003 extends ErrorCodeType { 
    override val description = "Operation requested is not currently supported by the service ot the device."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Include explanation.")
    override def toString = "SVC_S00_0003" }
  case object SVC_S00_0004 extends ErrorCodeType { 
    override val description = "Service unable to find/lookup device endpoint."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override def toString = "SVC_S00_0004" }
  case object SVC_S00_0005 extends ErrorCodeType { 
    override val description = "Job command failed."
    override val notes = Some("As only ever part of a fault notification, a status code is not applicable.")
    override def toString = "SVC_S00_0005" }
  case object SVC_S00_0006 extends ErrorCodeType { 
    override val description = "Queue command failed."
    override val notes = Some("As only ever part of a fault notification, a status code is not applicable.")
    override def toString = "SVC_S00_0006" }
  case object SVC_S00_0007 extends ErrorCodeType { 
    override val description = "Service unable to connect to device endpoint."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override def toString = "SVC_S00_0007" }
  case object SVC_S00_0008 extends ErrorCodeType { 
    override val description = "Job queue is full, locked or stopped. No new jobs are being accepted."
    override val statusCode = Some(503)
    override val statusDescription = Some("Service unavailable.")
    override def toString = "SVC_S00_0008" }
  case object SVC_S00_0009 extends ErrorCodeType { 
    override val description = "Job ended with a failure."
    override val notes = Some("As only ever part of a fault notification, a status code is not applicable.")
    override def toString = "SVC_S00_0009" }
  case object SVC_S00_0010 extends ErrorCodeType { 
    override val description = "Service received no response from device."
    override val statusCode = Some(504)
    override val statusDescription = Some("Gateway timeout.")
    override def toString = "SVC_S00_0010" }
  case object SVC_S00_0011 extends ErrorCodeType { 
    override val description = "Service received an exception from device. See description or exception detail."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override def toString = "SVC_S00_0011" }
  case object SVC_S00_0012 extends ErrorCodeType { 
    override val description = "Service received an unknown or an internal error from device. See description for error detail."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override def toString = "SVC_S00_0012" }
  case object SVC_S00_0013 extends ErrorCodeType { 
    override val description = "Unable to connect to client's notification service endpoint (replyTo) to send the asynchronous job result notification response."
    override val notes = Some("As only ever part of a notification, a status code is not applicable.")
    override def toString = "SVC_S00_0013" }
  case object SVC_S00_0014 extends ErrorCodeType { 
    override val description = "Unable to connect to client's service endpoint (faultTo) to send the asynchronous job fault response."
    override val notes = Some("Status code is not applicable. Usage only possible in log files.")
    override def toString = "SVC_S00_0014" }
  case object SVC_S00_0015 extends ErrorCodeType { 
    override val description = "Feature not supported."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S00_0015" }
  case object SVC_S00_0016 extends ErrorCodeType { 
    override val description = "Deadline passed."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S00_0016" }
  case object SVC_S00_0017 extends ErrorCodeType { 
    override val description = "Time constraints in request cannot be met."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S00_0017" }
  case object SVC_S00_0018 extends ErrorCodeType { 
    override val description = "Internal or unknown error encountered. See description for error detail."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override def toString = "SVC_S00_0018" }
  case object SVC_S00_0019 extends ErrorCodeType {
    override val description = "Version mismatch."
    override val statusCode = Some(412)
    override val statusDescription = Some("Precondition failed.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SVC_S00_0019" }
  case object SVC_S00_0020 extends ErrorCodeType {
    override val description = "License expired."
    override val statusCode = Some(502)
    override val statusDescription = Some("Bad gateway.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SVC_S00_0020" }
  case object SVC_S00_0021 extends ErrorCodeType {
    override val description = "Service state conflict."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SVC_S00_0021" }
  case object SVC_S00_0022 extends ErrorCodeType {
    override val description = "Operation not allowed."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some("Current state of the resource does not permit the requested operation.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SVC_S00_0022" }
  case object DAT_S00_0001 extends ErrorCodeType { 
    override val description = "Invalid request, XML format."
    override val statusCode = Some(400) 
    override val statusDescription = Some("Bad request.")
    override def toString = "DAT_S00_0001" }
  case object DAT_S00_0002 extends ErrorCodeType { 
    override val description = "Invalid input media format."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("This is not a 415 as the media data is not carried in the entity body.")
    override def toString = "DAT_S00_0002" }
  case object DAT_S00_0003 extends ErrorCodeType { 
    override val description = "Invalid jobID - the supplied jobID does not exist."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override def toString = "DAT_S00_0003" }
  case object DAT_S00_0004 extends ErrorCodeType { 
    override val description = "Missing required service metadata in request."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override def toString = "DAT_S00_0004" }
  case object DAT_S00_0005 extends ErrorCodeType { 
    override val description = "Duplicate jobID detected for new job."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override def toString = "DAT_S00_0005" }
  case object DAT_S00_0006 extends ErrorCodeType { 
    override val description = "Invalid request parameters."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override def toString = "DAT_S00_0006" }
  case object DAT_S00_0007 extends ErrorCodeType { 
    override val description = "Job command not valid."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Job state machine is in wrong state.")
    override def toString = "DAT_S00_0007" }
  case object DAT_S00_0008 extends ErrorCodeType { 
    override val description = "Queue command not valid."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden ")
    override def toString = "DAT_S00_0008" }
  case object DAT_S00_0009 extends ErrorCodeType { 
    override val description = "Invalid priority."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden")
    override def toString = "DAT_S00_0009" }
  case object DAT_S00_0010 extends ErrorCodeType { 
    override val description = "Input media not found. Invalid resource URI specified."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override def toString = "DAT_S00_0010" }
  case object DAT_S00_0011 extends ErrorCodeType { 
    override val description = "Duplicate resource."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some("Adding a duplicate resource.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "DAT_S00_0011" }
  case object DAT_S00_0012 extends ErrorCodeType { 
    override val description = "Invalid resource."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("Using a resource that does not exist.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "DAT_S00_0012" }
  case object DAT_S00_0013 extends ErrorCodeType { 
    override val description = "Invalid identifier."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Usage of invalid ResourceIDType.") // TODO ResourceIDType?
    override val introducedInVersion = Some("1_1_0")
    override def toString = "DAT_S00_0013" }
  case object DAT_S00_0014 extends ErrorCodeType { 
    override val description = "Insufficient data."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Resource passed with insufficient data. Service expects a minimum amount of properties to be populated " +
    		"as part of the operation.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "DAT_S00_0014" }
  case object EXT_S00_0000 extends ErrorCodeType { 
    override val description = "Extended code. See extended error code for details."
    override val notes = Some("Status code determined from extension.")
    override def toString = "EXT_S00_0000" }
  case object SEC_S00_0001 extends ErrorCodeType { 
    override val description = "Invalid credential."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0001" }
  case object SEC_S00_0002 extends ErrorCodeType { 
    override val description = "Credential required."
    override val statusCode = Some(401)
    override val statusDescription = Some("Unauthorized.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0002" }
  case object SEC_S00_0003 extends ErrorCodeType { 
    override val description = "Insufficient permission."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("According to their credentials, a user does not have enough permission to perform a specific operation.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0003" }
  case object SEC_S00_0004 extends ErrorCodeType { 
    override val description = "Invalid token."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("The token passed to the operation is not recognized by the system.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0004" }
  case object SEC_S00_0005 extends ErrorCodeType { 
    override val description = "Missing token."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("The operation expects a token to be passed and it cannot be empty or null.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0005" }
  case object SEC_S00_0006 extends ErrorCodeType { 
    override val description = "Resource locked."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some(
        "Trying to lock a resource that is already locked by another token. Triggered either when: " + 
    		"the content that includes this essence is being locked by another token, or; " + 
    		"an essence in the content is being locked by another token.")
    override val introducedInVersion = Some("1_1_0")
    override def toString = "SEC_S00_0006" }
}

object MakeBaseErrors extends App {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  def makeSingleEntry(errorCode: ErrorCodeDetails) = {
    <enumeration value={Text(errorCode.toString)}>
	  <annotation>{
	    <documentation source="urn:x-fims:description">{Text(errorCode.description)}</documentation> ++
	    (errorCode.statusCode map {code => <documentation source="urn:x-fims:statusCode">{Text(code.toString)}</documentation>} getOrElse(NodeSeq.Empty)) ++
	    (errorCode.statusDescription map {description => <documentation source="urn:x-fims:statusDescription">{Text(description)}</documentation>}getOrElse(NodeSeq.Empty)) ++
	    (errorCode.notes map {notes => <documentation source="urn:x-fims:notes">{Text(notes)}</documentation>} getOrElse(NodeSeq.Empty)) ++
	    (errorCode.introducedInVersion map {newIn => <documentation source="urn:x-fims:introducedInVersion">{Text(newIn)}</documentation>} getOrElse(NodeSeq.Empty)) ++ 
	    (errorCode.modifiedInVersion map {modified => <documentation source="urn:x-fims:modifiedInVersion">{Text(modified)}</documentation>} getOrElse(NodeSeq.Empty)) }
	  </annotation>
	</enumeration>
  }
  def makeAllErrorCodes() = {
    <simpleType name="ErrorCodeType">
      <annotation>
	    <documentation source="urn:x-fims:description">Common error codes which can be shared by
		  different classes of adapters. Error codes are classified in five main categories: 
		    • INF_S00_xxxx: Infrastructure errors (system, storage, network, memory, processor)
		    • DAT_S00_xxxx: Data errors (validation, missing, duplication)
		    • SVC_S00_xxxx: Operation errors (existence, support, lock, connection, failure)
		    • SEC_S00_xxxx: Security errors (authentication, authorization)
			• EXT_S00_xxxx: Extended code. See extended error code for detail. </documentation>
		<documentation source="urn:x-fims:modifiedInVersion">1_1_0</documentation>
	  </annotation>
	  <restriction base="string">{
        ErrorCodeType.allCodes map {makeSingleEntry(_) } }
      </restriction>
    </simpleType>
  }
  println(printer.format(makeAllErrorCodes()))
}

trait FimsException extends RuntimeException {
  val errorCode: ErrorCodeDetails
  override def getMessage() = errorCode.description
}

class SystemUnavailable extends FimsException { val errorCode = ErrorCodeType.INF_S00_0001 }
class SystemTimeout extends FimsException { val errorCode = ErrorCodeType.INF_S00_0002 }
class SystemInternalError extends FimsException { val errorCode = ErrorCodeType.INF_S00_0003 }
class DatabaseConnectionError extends FimsException { val errorCode = ErrorCodeType.INF_S00_0004 }
class SystemOutOfMemory extends FimsException { val errorCode = ErrorCodeType.INF_S00_0005 }
class SystemOutOfDiskSpace extends FimsException { val errorCode = ErrorCodeType.INF_S00_0006 }
class MaximumConnectionsReached extends FimsException { val errorCode = ErrorCodeType.INF_S00_0007 }
class JobCommandNotSupported extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0001 }
class QueueCommandNotSupported extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0002 }
class OperationRequestedIsNotSupported extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0003 }
class ServiceUnableToFindEndpoint extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0004 }
class JobCommandFailed extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0005 }
class QueueCommandFailed extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0006 }
class ServiceUnableToConnectToEndpoint extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0007 }
class NoNewJobsAreBeingAccepted extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0008 }
class JobEndedWithFailure extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0009 }
class ServiceReceivedNoResponse extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0010 }
class ServiceReceivedException extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0011 }
class ServiceReceivedError extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0012 }
class UnableToConnectReplyTo extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0013 }
class UnableToConnectFaultTo extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0014 }
class FeatureNotSupported extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0015 }
class DeadlinePassed extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0016 }
class ImpossibleTimeConstraints extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0017 }
class InternalOrUnknownError extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0018 }
class VersionMismatch extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0019 }
class LicenseExpired extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0020 }
class ServiceStateConflict extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0021 }
class OperationNotAllowed extends FimsException { val errorCode = ErrorCodeType.SVC_S00_0022 }
class InvalidRequestXMLFormat extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0001 }
class InvalidInputMediaFormat extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0002 }
class InvalidJobID extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0003 }
class MissingRequiredRequest extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0004 }
class DuplicateJobIDDetected extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0005 }
class InvalidRequestParameters extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0006 }
class JobCommandNotValid extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0007 }
class QueueCommandNotValid extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0008 }
class InvalidPriority extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0009 }
class InputMediaNotFound extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0010 }
class DuplicateResource extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0011 }
class InvalidResource extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0012 }
class InvalidIdentifier extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0013 }
class InsufficientData extends FimsException { val errorCode = ErrorCodeType.DAT_S00_0014 }
class ExtendedCode extends FimsException { val errorCode = ErrorCodeType.EXT_S00_0000 }
class InvalidCredential extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0001 }
class CredentialRequired extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0002 }
class InsufficientPermission extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0003 }
class InvalidToken extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0004 }
class MissingToken extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0005 }
class ResourceLocked extends FimsException { val errorCode = ErrorCodeType.SEC_S00_0006 }

