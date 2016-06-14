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
import tv.amwa.ebu.fims.rest.model.Length
import tv.amwa.ebu.fims.rest.model.ContainerFormatParameters
import tv.amwa.ebu.fims.rest.model.TechnicalAttribute
import tv.amwa.ebu.fims.rest.model.TransferAtom
import tv.amwa.ebu.fims.rest.model.BMEssenceLocatorType
import akka.util.ByteString
import tv.amwa.ebu.fims.rest.model.BMContentFormatType
import tv.amwa.ebu.fims.rest.model.BMContentType
import tv.amwa.ebu.fims.rest.model.DescriptionType
import tv.amwa.ebu.fims.rest.model.BMTrack
import tv.amwa.ebu.fims.rest.model.CaptioningFormat
import tv.amwa.ebu.fims.rest.model.AncillaryDataFormat
import tv.amwa.ebu.fims.rest.model.BMObjectType
import tv.amwa.ebu.fims.rest.model.ProfileType
import com.sun.xml.internal.ws.wsdl.writer.document.FaultType
import tv.amwa.ebu.fims.rest.model.InnerFault

object ParameterConverters {
  implicit def stringToSomeURI(uri: String): Option[java.net.URI] = Some(new java.net.URI(uri))
  implicit def stringToURI(uri: String): java.net.URI = new java.net.URI(uri)
  implicit def longToSomeLong(value: Long): Option[Long] = Some(value)
  implicit def doubleToSomeDouble(value: Double): Option[Double] = Some(value)
  implicit def intToSomeInt(value: Int): Option[Int] = Some(value)
  implicit def booleanToSomeBoolean(value: Boolean): Option[Boolean] = Some(value)
  implicit def definedToSome[T <: AnyRef](value: T): Option[T] = Some(value)
  implicit def longToSomeLength(value: Long): Option[Length] = Some(Length(value))
  implicit def stringToSomeContainerFormatParameters(value: String): Option[ContainerFormatParameters] = Some(ContainerFormatParameters(value))
  implicit def technicalAttributeToListOf(value: TechnicalAttribute): Seq[TechnicalAttribute] = List(value)
  implicit def transferAtomToListOf(value: TransferAtom): Seq[TransferAtom] = List(value)
  implicit def locatorToListOfLocator(value: BMEssenceLocatorType): Seq[BMEssenceLocatorType] = List(value)
  implicit def contentFormatToListOfContentFormat(value: BMContentFormatType): Seq[BMContentFormatType] = List(value)
  implicit def contentFormatToListOfContent(value: BMContentType): Seq[BMContentType] = List(value)
  implicit def descriptionToListOfDescription(value: DescriptionType): Seq[DescriptionType] = List(value)
  implicit def bmTrackToListOfDescription(value: BMTrack): Seq[BMTrack] = List(value)
  implicit def captioningFormatToListOfCaptioningFormat(value: CaptioningFormat): Seq[CaptioningFormat] = List(value)
  implicit def ancillaryDataFormatToListOfAncillaryDataFormat(value: AncillaryDataFormat): Seq[AncillaryDataFormat] = List(value)
  implicit def bmObjectToListOfBMObject(value: BMObjectType): Seq[BMObjectType] = List(value)
  implicit def profileToListOfProfile[T <: ProfileType](value: T): Seq[T] = List(value)
  implicit def faultToListOfFault(value: InnerFault): Seq[InnerFault] = List(value)
  implicit def stringToByteString(value: String): ByteString = HexBinary.fromHex(value)
}