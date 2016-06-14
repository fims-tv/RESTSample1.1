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

package tv.amwa.ebu.fims.rest.model.transform

import tv.amwa.ebu.fims.rest.model.FaultType
import tv.amwa.ebu.fims.rest.model.ErrorCodeType
import tv.amwa.ebu.fims.rest.model.InnerFault
import tv.amwa.ebu.fims.rest.model.ErrorCodeDetails
import tv.amwa.ebu.fims.rest.model.MakeBaseErrors
import tv.amwa.ebu.fims.rest.model.FimsException

trait TransformFaultType extends FaultType {
  val extendedCode: Option[TransformErrorCodeType]
}

case class TransformFault(
    code: ErrorCodeType,
    description: Option[String] = None,
    detail: Option[String] = None,
    innerFault: Seq[InnerFault] = Nil,
    extendedCode: Option[TransformErrorCodeType] = None) extends TransformFaultType
    
sealed trait TransformErrorCodeType extends ErrorCodeDetails

object TransformErrorCodeType {
  def fromString(value: String): TransformErrorCodeType = value match {
    case "SVC_S02_0001" => SVC_S02_0001
    case _ => throw new IllegalArgumentException("Status code '" + value + "' is not a known transform error code.")
  }
  val allCodes: List[TransformErrorCodeType] = List(SVC_S02_0001)
  case object SVC_S02_0001 extends TransformErrorCodeType {
    override val description = "Invalid target media format."
    override val statusCode = Some(403)
    override val statusDescription = Some("Forbidden.")
    override def toString = "SVC_S02_0001"
  }
}

class InvalidTargetMediaFormat extends FimsException { val errorCode = TransformErrorCodeType.SVC_S02_0001 }

object TransformErrorSchemaGen extends App {
  val printer = new scala.xml.PrettyPrinter(1000, 2)
  def makeAllErrorCodes() = {
    <simpleType name="TransformErrorCodeType">
	  <annotation>
	    <documentation source="urn:x-fims:description">Specific error codes for the transform service:
		  - INF_S02_xxxx: Infrastructure errors (system, storage, network, memory, processor)
		  - DAT_S02_xxxx: Data errors (validation, missing,  duplication)
		  - SVC_S02_xxxx: Operation errors (existence, support, lock, connection, failure)
		  - SEC_S02_xxxx: Security errors (authentication, authorization)</documentation>
	    <documentation source="urn:x-fims:normativeRequirement"/>
		<documentation source="urn:x-fims:serviceDescription"/>
		<documentation source="urn:x-fims:contentOfServiceDescription"/>
      </annotation>
	  <restriction base="string">{
	    TransformErrorCodeType.allCodes map {MakeBaseErrors.makeSingleEntry(_) } }
	  </restriction>
	</simpleType>
  }
  println(printer.format(makeAllErrorCodes()))
}