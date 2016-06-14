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

package tv.amwa.ebu.fims.rest.model.capture
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import tv.amwa.ebu.fims.rest.model.InnerFault
import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeDetails
import tv.amwa.ebu.fims.rest.model.MakeBaseErrors
import tv.amwa.ebu.fims.rest.model.FimsException

trait CaptureFaultType extends FaultType {
  val extendedCode: Option[CaptureErrorCodeType]
}

/** Fault information for the capture media service. It extends the 
 *  base FaultType with a complementary extended code that allows service-specific 
 *  error codes to be included if needed. If 
 *  an exception is generated when the capture request message is submitted to the service, 
 *  it shall respond with a message based on the CaptureFaultType.
*/
case class CaptureFault(
    code: ErrorCodeType,
    description: Option[String] = None,
    detail: Option[String] = None,
    innerFault: Seq[InnerFault] = Nil,
    extendedCode: Option[CaptureErrorCodeType] = None) extends CaptureFaultType
    
sealed trait CaptureErrorCodeType extends ErrorCodeDetails

object CaptureErrorCodeType {
  def fromString(value: String): CaptureErrorCodeType = value match {
    case "SVC_S03_0001" => SVC_S03_0001
    case "SVC_S03_0002" => SVC_S03_0002
    case "DAT_S03_0001" => DAT_S03_0001
    case _ => throw new IllegalArgumentException("Status code '" + value + "' is not a known capture error code.")
  }
  val allCodes: List[CaptureErrorCodeType] = List(SVC_S03_0001, SVC_S03_0002, DAT_S03_0001)
  case object SVC_S03_0001 extends CaptureErrorCodeType {
    override val description = "Invalid target media format."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S03_0001" } 
  case object SVC_S03_0002 extends CaptureErrorCodeType {
    override val description = "Inconsistent time constraints."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S03_0002" } 
  case object DAT_S03_0001 extends CaptureErrorCodeType {
    override val description = "Invalid source ID."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "DAT_S03_0001" } 
}

class InvalidTargetMediaFormat extends FimsException { val errorCode = CaptureErrorCodeType.SVC_S03_0001 }
class InconsistentTimeConstraints extends FimsException { val errorCode = CaptureErrorCodeType.SVC_S03_0002 }
class InvalidSourceID extends FimsException { val errorCode = CaptureErrorCodeType.DAT_S03_0001 }

object CaptureErrorSchemaGen extends App {
    val printer = new scala.xml.PrettyPrinter(1000, 2)
  def makeAllErrorCodes() = {
    <simpleType name="CaptureErrorCodeType">
      <annotation>
	    <documentation source="urn:x-fims:description">Specific error codes for the
		  capture service:
		    - INF_S03_xxxx: Infrastructure errors (system, storage, network, memory, processor)
		    - DAT_S03_xxxx: Data errors (validation, missing, duplication)
			- SVC_S03_xxxx: Operation errors (existence, support, lock, connection, failure)
			- SEC_S03_xxxx: Security errors (authentication, authorization)
		  </documentation>
		  <documentation source="urn:x-fims:normativeRequirement"/>
		  <documentation source="urn:x-fims:serviceDescription"/>
		  <documentation source="urn:x-fims:contentOfServiceDescription"/>
		</annotation>
		<restriction base="string">{
	     CaptureErrorCodeType.allCodes map {MakeBaseErrors.makeSingleEntry(_) } }
	  </restriction>
	</simpleType>
  }
  println(printer.format(makeAllErrorCodes()))
}


