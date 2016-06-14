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

package tv.amwa.ebu.fims.rest.model.repository

import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import tv.amwa.ebu.fims.rest.model.InnerFault
import tv.amwa.ebu.fims.rest.model.ErrorCodeDetails
import tv.amwa.ebu.fims.rest.model.MakeBaseErrors

trait RepositoryFaultType extends FaultType {
  val extendedCode: Option[RepositoryErrorCodeType]
}

case class RepositoryFault(
    code: ErrorCodeType,
    description: Option[String] = None,
    detail: Option[String] = None,
    innerFault: Seq[InnerFault] = Nil,
    extendedCode: Option[RepositoryErrorCodeType] = None) extends RepositoryFaultType
    
sealed trait RepositoryErrorCodeType extends ErrorCodeDetails

object RepositoryErrorCodeType {
  def fromString(value: String): RepositoryErrorCodeType = value match {
    case "SVC_S04_0001" => SVC_S04_0001
    case "SVC_S04_0002" => SVC_S04_0002
    case "SVC_S04_0003" => SVC_S04_0003
    case "SVC_S04_0004" => SVC_S04_0004
    case "SVC_S04_0005" => SVC_S04_0005
    case "SVC_S04_0006" => SVC_S04_0006
    case "SVC_S04_0007" => SVC_S04_0007
    case "SVC_S04_0008" => SVC_S04_0008
    case "SVC_S04_0009" => SVC_S04_0009
    case "DAT_S04_0001" => DAT_S04_0001
    case "DAT_S04_0002" => DAT_S04_0002
    case "DAT_S04_0003" => DAT_S04_0003
    case "DAT_S04_0004" => DAT_S04_0004
    case "DAT_S04_0005" => DAT_S04_0005
    case "DAT_S04_0006" => DAT_S04_0006
    case "DAT_S04_0007" => DAT_S04_0007
    case "DAT_S04_0008" => DAT_S04_0008
    case "DAT_S04_0009" => DAT_S04_0009
    case "DAT_S04_0010" => DAT_S04_0010
    case "DAT_S04_0011" => DAT_S04_0011
    case "DAT_S04_0012" => DAT_S04_0012
    case "DAT_S04_0013" => DAT_S04_0013
    case "INF_S04_0001" => INF_S04_0001
    case _ => throw new IllegalArgumentException("Error status code '" + value + "' does not match a known repository error code.")
  }
  val allCodes: List[RepositoryErrorCodeType] = List(
      SVC_S04_0001, SVC_S04_0002, SVC_S04_0003, SVC_S04_0004,
      SVC_S04_0005, SVC_S04_0006, SVC_S04_0007, SVC_S04_0008,
      SVC_S04_0009, DAT_S04_0001, DAT_S04_0002, DAT_S04_0003,
      DAT_S04_0004, DAT_S04_0005, DAT_S04_0006, DAT_S04_0007,
      DAT_S04_0008, DAT_S04_0009, DAT_S04_0010, DAT_S04_0011,
      DAT_S04_0012, DAT_S04_0013, INF_S04_0001)
  case object SVC_S04_0001 extends RepositoryErrorCodeType {
    override val description = "Change not allowed."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Updating properties not allowed to be updated by the service provider in the operation.")
    override def toString = "SVC_S04_0001" } 
  case object SVC_S04_0002 extends RepositoryErrorCodeType {
    override val description = "Excessive data."
    override val statusCode = Some(202)
    override val statusDescription = Some("Accepted.")
    override val notes = Some("Some of the data passed will be ignored by the system during this operation. In this case, a warning rather than " +
    		"a fault is issued in the response.")
    override def toString = "SVC_S04_0002" } 
  case object SVC_S04_0003 extends RepositoryErrorCodeType {
    override val description = "Format properties extraction."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override val notes = Some("Unable to extract the technical metadata (format) properties for an essence. The request is OK but the " +
    		"essence is an unknwon format or is missing some details.")
    override def toString = "SVC_S04_0003" } 
  case object SVC_S04_0004 extends RepositoryErrorCodeType {
    override val description = "Incomplete essence."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The essence is still processing and is not fully copied to the source location. The status is 404 now but " +
    		"is expected to be 200 in a while.")
    override def toString = "SVC_S04_0004" } 
  case object SVC_S04_0005 extends RepositoryErrorCodeType {
    override val description = "Property action not allowed."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override val notes = Some("Repository action types new, update or remove are not supported for the given property")
    override def toString = "SVC_S04_0005" } 
  case object SVC_S04_0006 extends RepositoryErrorCodeType {
    override val description = "Property not defined."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Property is not part of updatable properties in the RCR.")
    override def toString = "SVC_S04_0006" } 
  case object SVC_S04_0007 extends RepositoryErrorCodeType {
    override val description = "Property path not found."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The given XPath is invalid. The XPath is viewed as an extension of the API.")
    override def toString = "SVC_S04_0007" } 
  case object SVC_S04_0008 extends RepositoryErrorCodeType {
    override val description = "Property value not supported."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("The given custom value is not supported.")
    override def toString = "SVC_S04_0008" } 
  case object SVC_S04_0009 extends RepositoryErrorCodeType {
    override val description = "RCR parameter violation."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Generic RCR values violation, e.g. maximum number of query operands violation.")
    override def toString = "SVC_S04_0009" } 
  case object DAT_S04_0001 extends RepositoryErrorCodeType {
    override val description = "Duplicate content."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some("Adding a content that already exists.")
    override def toString = "DAT_S04_0001" } 
  case object DAT_S04_0002 extends RepositoryErrorCodeType {
    override val description = "Duplicate essence."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some("Adding an Essence that already exists.")
    override def toString = "DAT_S04_0002" } 
  case object DAT_S04_0003 extends RepositoryErrorCodeType {
    override val description = "Essence not found."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The essence is not found in the given location.")
    override def toString = "DAT_S04_0003" } 
  case object DAT_S04_0004 extends RepositoryErrorCodeType {
    override val description = "Essence size exceeded."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override val notes = Some("The system supports essences up to a certain size.")
    override def toString = "DAT_S04_0004" } 
  case object DAT_S04_0005 extends RepositoryErrorCodeType {
    override val description = "External reference violation."
    override val statusCode = Some(409)
    override val statusDescription = Some("Conflict.")
    override val notes = Some("Resource being referenced by external resource and can not be removed.")
    override def toString = "DAT_S04_0005" } 
  case object DAT_S04_0006 extends RepositoryErrorCodeType {
    override val description = "Internal reference violation."
    override val statusCode = Some(500)
    override val statusDescription = Some("Internal server error.")
    override val notes = Some("Cross content or cross format usage.")
    override def toString = "DAT_S04_0006" } 
  case object DAT_S04_0007 extends RepositoryErrorCodeType {
    override val description = "Invalid BMContentType."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The content does not exist.")
    override def toString = "DAT_S04_0007" } 
  case object DAT_S04_0008 extends RepositoryErrorCodeType {
    override val description = "Invalid essence."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The essence does not exist.")
    override def toString = "DAT_S04_0008" } 
  case object DAT_S04_0009 extends RepositoryErrorCodeType {
    override val description = "Invalid essence structure."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("The EssenceLocator representation and structure is not valid or is missing information.")
    override def toString = "DAT_S04_0009" } 
  case object DAT_S04_0010 extends RepositoryErrorCodeType {
    override val description = "Invalid expiration date."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Setting lock expiraion date in the past.")
    override def toString = "DAT_S04_0010" } 
  case object DAT_S04_0011 extends RepositoryErrorCodeType {
    override val description = "Invalid format."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("The format data is not valid.")
    override def toString = "DAT_S04_0011" } 
  case object DAT_S04_0012 extends RepositoryErrorCodeType {
    override val description = "Invalid query input definition."
    override val statusCode = Some(400)
    override val statusDescription = Some("Bad request.")
    override val notes = Some("Bad query.")
    override def toString = "DAT_S04_0012" } 
  case object DAT_S04_0013 extends RepositoryErrorCodeType {
    override val description = "Invalid registered location."
    override val statusCode = Some(404)
    override val statusDescription = Some("Not found.")
    override val notes = Some("The well known location is not known. The RCR includes essence loators types to use when adding essence.")
    override def toString = "DAT_S04_0013" } 
  case object INF_S04_0001 extends RepositoryErrorCodeType {
    override val description = "Max operation duration exceeded."
    override val statusCode = Some(408)
    override val statusDescription = Some("Request timeout.")
    override val notes = Some("Time out of executed operation.")
    override def toString = "INF_S04_0001" } 
}

object RepositoryErrorSchemaGen extends App {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  def makeAllErrorCodes() = {
    <simpleType name="RepositoryErrorCodeType">
      <annotation>
	    <documentation source="urn:x-fims:description">Specific error codes for the
		  repository service:
		    - INF_S04_xxxx: Infrastructure errors (system, storage, network, memory, processor)
		    - DAT_S04_xxxx: Data errors (validation, missing, duplication)
			- SVC_S04_xxxx: Operation errors (existence, support, lock, connection, failure)
			- SEC_S04_xxxx: Security errors (authentication, authorization)
		  </documentation>
		  <documentation source="urn:x-fims:normativeRequirement"/>
		  <documentation source="urn:x-fims:serviceDescription"/>
		  <documentation source="urn:x-fims:contentOfServiceDescription"/>
	  	  <documentation source="utn:x-fims:introducedInVersion">1_1_0</documentation>
		</annotation>
		<restriction base="string">{
	     RepositoryErrorCodeType.allCodes map {MakeBaseErrors.makeSingleEntry(_) } }
	  </restriction>
	</simpleType>
  }
  println(printer.format(makeAllErrorCodes()))
}