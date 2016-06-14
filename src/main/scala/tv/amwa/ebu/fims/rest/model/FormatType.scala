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
import scala.xml.Attribute

/** Characteristics of files, video, audio and ancillary data.
*/
trait FormatType extends ResourceType {
  val technicalAttribute: Seq[TechnicalAttribute]
}

trait VideoFormatType extends FormatType {
  val displayWidth: Option[Length]
  val displayHeight: Option[Length]
  val frameRate: Option[Rational]
  val aspectRatio: Option[Rational]
  val videoEncoding: Option[Codec]
  val videoTrack: Seq[BMTrack]
  val bitRate: Option[Long]
  val bitRateMode: Option[BitRateModeType]
  val lines: Option[Int]
  val scanningFormat: Option[ScanningFormatType]
  val scanningOrder: Option[ScanningOrderType]
  val noiseFilter: Option[Boolean]
  require(bitRate.map(_ >= 0).getOrElse(true))
  require(lines.map(_ >= 0).getOrElse(true))
}

/** Characteristics of a video signal. See 'videoFormat' in EBU Tech 3293 for more information. */
case class VideoFormat(
    resourceParameters: ResourceParameters,
    technicalAttribute: Seq[TechnicalAttribute] = Nil,
    displayWidth: Option[Length] = None,
    displayHeight: Option[Length] = None,
    frameRate: Option[Rational] = None,
    aspectRatio: Option[Rational] = None,
    videoEncoding: Option[Codec] = None,
    videoTrack: Seq[BMTrack] = Nil,
    bitRate: Option[Long] = None,
    bitRateMode: Option[BitRateModeType] = None,
    lines: Option[Int] = None,
    scanningFormat: Option[ScanningFormatType] = None,
    scanningOrder: Option[ScanningOrderType] = None,
    noiseFilter: Option[Boolean] = None) extends VideoFormatType
    
trait AudioFormatType extends FormatType {
  val samplingRate: Option[Double]
  val audioEncoding: Option[Codec]
  val trackConfiguration: Option[TrackConfiguration]
  val audioTrack: Seq[BMTrack]
  val channels: Option[Int]
  val bitRate: Option[Long]
  val bitRateMode: Option[BitRateModeType]
  val sampleSize: Option[Int]
  val sampleType: Option[AudioSampleType]
  require(channels.map{_ >= 0}.getOrElse(true))
  require(bitRate.map{_ >= 0l}.getOrElse(true))
  require(sampleSize.map{_ >= 0}.getOrElse(true))
}

/** Characteristics of an audio signal. See 'audioFormat' in EBU Tech 3293 for more information. */
case class AudioFormat(
    resourceParameters: ResourceParameters,
    technicalAttribute: Seq[TechnicalAttribute] = Nil,
    samplingRate: Option[Double] = None,
    audioEncoding: Option[Codec] = None,
    trackConfiguration: Option[TrackConfiguration] = None,
    audioTrack: Seq[BMTrack] = Nil,
    channels: Option[Int] = None,
    bitRate: Option[Long] = None,
    bitRateMode: Option[BitRateModeType] = None,
    sampleSize: Option[Int] = None,
    sampleType: Option[AudioSampleType] = None) extends AudioFormatType

trait ContainerFormatType extends FormatType {
  val containerFormat: Option[ContainerFormatParameters]
}
    
/** Container/wrapper format that is used in complement to the streams encoding, for example MXF, wave, 
 *  Quicktime, etc.. See 'fileFormat' in EBU Tech 3293 for more information.
*/
case class ContainerFormat(
    resourceParameters: ResourceParameters,
    technicalAttribute: Seq[TechnicalAttribute] = Nil,
    containerFormat: Option[ContainerFormatParameters] = None) extends ContainerFormatType

trait DataFormatType extends FormatType {
  val captioningFormat: Seq[CaptioningFormat]
  val ancillaryDataFormat: Seq[AncillaryDataFormat]
}
    
/** Characteristics of a data signal, used to carry captioning or ancillary data. */
case class DataFormat(
    resourceParameters: ResourceParameters,
    technicalAttribute: Seq[TechnicalAttribute] = Nil,
    captioningFormat: Seq[CaptioningFormat] = Nil,
    ancillaryDataFormat: Seq[AncillaryDataFormat] = Nil) extends DataFormatType

/** Ancillary data packet type. See SMPTE ST
				291 and SMPTE ST 436.
*/
case class AncillaryDataFormat(
    DID: Option[Int] = None,
    SDID: Option[Int] = None,
    lineNumber: Option[Int] = None,
    wrappingType: Option[Int] = None
//  ExtensionGroup: Option[com.quantel.qstack.industry.model.ExtensionGroup] = None,
//  ExtensionAttributes: Option[com.quantel.qstack.industry.model.ExtensionAttributes] = None)    
)

/** A length value and its unit of measurement. */
case class Length(
    value: Long,
    unit: Option[String] = None) {
  require(value >= 0)
}

/** A rational value expressed by its fraction of a second numerator and denominator components. 
 *  Rational values can be used to represent edit rates. For example, a frame rate of 29.97 would 
 *  be represented as 30 corrected by 1000 (numerator) / 1001 (denominator). 
*/
case class Rational(
    value: Long,
    numerator: Int,
    denominator: Int) {
  require(denominator != 0)
  def this(value: Long) = this(value, 1, 1)
}

object Rational {
  def apply(value: Long) = new Rational(value, 1, 1)
}
      
sealed trait BitRateModeType

object BitRateModeType {
  def fromString(value: String): BitRateModeType = value match {
    case "constant" => Constant
    case "variable" => Variable
  }
}

case object Constant extends BitRateModeType { override def toString = "constant" }
case object Variable extends BitRateModeType { override def toString = "variable" }

sealed trait ScanningFormatType

object ScanningFormatType {
  def fromString(value: String): ScanningFormatType = value match {
    case "interlaced" => Interlaced
    case "progressive" => Progressive
  }
}

case object Interlaced extends ScanningFormatType { override def toString = "interlaced" }
case object Progressive extends ScanningFormatType { override def toString = "progressive" }

sealed trait ScanningOrderType

object ScanningOrderType {
  def fromString(value: String): ScanningOrderType = value match {
    case "top" => Top
    case "bottom" => Bottom
  }
}

case object Top extends ScanningOrderType { override def toString = "top" }
case object Bottom extends ScanningOrderType { override def toString = "bottom" }

trait TypeGroup[T <: TypeGroup[T]] {
  val typeLabel: Option[String]
  val typeDefinition: Option[String]
  val typeLink: Option[java.net.URI]
  def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) : T
}

trait FormatGroup[T <: FormatGroup[T]] {
  val formatLabel: Option[String]
  val formatDefinition: Option[String]
  val formatLink: Option[java.net.URI]
  def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI]) : T
}

/** Describes a codec used for video or audio encoding. */
case class Codec(
    name: Option[String] = None,
    vendor: Option[String] = None,
    version: Option[String] = None,
    family: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None) extends TypeGroup[Codec] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    this.copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
}

/** Allows users / implementers to define their own technical parameters as a string for which a format can be 
 * defined to restrict the string format. See 'TechnicalAttributeString' in EBU Tech 3293 for more information.
*/
case class TechnicalAttribute(
    value: String,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    formatLabel: Option[String] = None,
    formatDefinition: Option[String] = None,
    formatLink: Option[java.net.URI] = None) extends TypeGroup[TechnicalAttribute] with FormatGroup[TechnicalAttribute] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    this.copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
  override def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI]) =
    copy(formatLabel = formatLabel, formatDefinition = formatDefinition, formatLink = formatLink) 
}
    
/** Tracks expose the underlying structural metadata of the content streams embedded inside a 
 *  physical content essence. Examples of tracks are the separate audio and video streams inside 
 *  an audiovisual content essence. Tracks have a category, for example "main" (videoTrack), 
 *  audioDescription" (audioTrack), "closed captioning" (dataTrack). Tracks have also an identifier 
 *  and a description.
*/
case class BMTrack(
//    ExtensionGroup: Option[com.quantel.qstack.industry.model.ExtensionGroup] = None,
//    ExtensionAttributes: Option[com.quantel.qstack.industry.model.ExtensionAttributes] = None,
    trackID: Option[ResourceID] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    trackName: Option[String] = None,
    language: Option[String] = None) extends TypeGroup[BMTrack] {
  require(language.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    this.copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
  
}

case class TrackConfiguration(
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None) extends TypeGroup[TrackConfiguration] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink)
}

/** Describes a captioning format and its purpose. */
case class CaptioningFormat(
    value: String,
    formatLabel: Option[String] = None,
    formatDefinition: Option[String] = None,
    formatLink: Option[java.net.URI] = None,
    captioningSourceUri: Option[java.net.URI] = None,
    language: Option[String] = None,
    attributes: Option[Attribute] = None) extends FormatGroup[CaptioningFormat] {
  require(language.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
  override def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI]) =
    copy(formatLabel = formatLabel, formatDefinition = formatDefinition, formatLink = formatLink) 
}

trait AudioSampleType

object AudioSampleType {
  def fromString(value: String): AudioSampleType = value match {
    case "float" => FloatType
    case "integer" => IntegerType

  }
}

case object FloatType extends AudioSampleType { override def toString = "float" }
case object IntegerType extends AudioSampleType { override def toString = "integer" }

case class ContainerFormatParameters(
    value: String,
    formatLabel: Option[String] = None,
    formatDefinition: Option[String] = None,
    formatLink: Option[java.net.URI] = None) extends FormatGroup[ContainerFormatParameters] {
  override def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI]) =
    copy(formatLabel = formatLabel, formatDefinition = formatDefinition, formatLink = formatLink) 
}
