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
import scala.xml.NodeSeq
import scala.xml.NodeSeq.Empty
import scala.xml.Text
import org.eclipse.jetty.io.NetworkTrafficListener.Empty

trait DescriptionType extends ResourceType {
  val bmContentDescription: Option[BMContentDescription]
}

case class Description(
    resourceParameters: ResourceParameters,
    bmContentDescription: Option[BMContentDescription] = None) extends DescriptionType    
    
case class BMContentDescription(
    val title: Seq[TextElement] = Nil,
    val alternativeTitle: Seq[Title] = Nil,
    val creator: Seq[Entity] = Nil,
    val subject: Seq[Subject] = Nil,
    val description: Seq[ContentDescription] = Nil,
    val publisher: Seq[Entity] = Nil,
    val contributor: Seq[Entity] = Nil,
    val date: Seq[Date] = Nil,
    val typeValue: Seq[Type] = Nil,
    val identifier: Seq[Identifier] = Nil,
    val language: Seq[Language] = Nil,
    val coverage: Seq[Coverage] = Nil,
    val rights: Seq[Rights] = Nil,
    val version: Option[String] = None,
    val lang: Option[String] = None) {
  require(lang.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
}
  
trait TextElementType {
  val value: String
  val lang: Option[String]
  require(lang.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
} 
  
case class TextElement(
    value: String,
    lang: Option[String] = None) extends TextElementType

trait DescriptionTypeGroup[T <: DescriptionTypeGroup[T]] extends TypeGroup[T] {
  val typeLanguage: Option[String]
  require(typeLanguage.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
  def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]): T
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI]): T =
    setTypeParameters(typeLabel, typeDefinition, typeLink, None)
}

trait DescriptionFormatGroup[T <: DescriptionFormatGroup[T]] extends FormatGroup[T] {
  val formatLanguage: Option[String]
  require(formatLanguage.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
  def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI], formatLanguage: Option[String]) : T
  def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI]): T =
    setFormatParameters(formatLabel, formatDefinition, formatLink, None)
}

case class Title(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[Title] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class ContentDescription(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[ContentDescription] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class Identifier(
    value: String,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None,
    formatLabel: Option[String] = None,
    formatDefinition: Option[String] = None,
    formatLink: Option[java.net.URI] = None,
    formatLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[Identifier] with DescriptionFormatGroup[Identifier] {
  override lazy val lang = None
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
  override def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI], formatLanguage: Option[String]) =
    copy(formatLabel = formatLabel, formatDefinition = formatDefinition, formatLink = formatLink, formatLanguage = formatLanguage)
}

case class Subject(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[Subject] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
 
}
case class Entity(
    contactDetails: Seq[ContactDetails],
    organisationDetails: Seq[OrganisationDetails],
    role: Seq[Role],
    entityId: Option[java.net.URI])
    
sealed trait ContactDetails {
    val username: Seq[TextElement]
    val occupation: Option[TextElement]
    val details: Seq[Details]
    val stageName: Seq[TextElement]
    val relatedContacts: Seq[Entity]
    val entityId: Option[java.net.URI]
    val typeLabel: Option[String]
    val typeDefinition: Option[String]
    val typeLink: Option[java.net.URI]
    val typeLanguage: Option[String]
}

case class SimpleNameContactDetails(
    name: String,
    username: Seq[TextElement] = Nil,
    occupation: Option[TextElement] = None,
    details: Seq[Details] = Nil,
    stageName: Seq[TextElement] = Nil,
    relatedContacts: Seq[Entity] = Nil,
    entityId: Option[java.net.URI] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends ContactDetails with DescriptionTypeGroup[SimpleNameContactDetails] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class DetailedNameContactDetails(
    givenName: Option[String] = None,
    familyName: Option[String] = None,
    otherGivenName: Seq[String] = Nil,
    suffix: Option[String] = None,
    salutation: Option[String] = None,
    username: Seq[TextElement] = Nil,
    occupation: Option[TextElement] = None,
    details: Seq[Details] = Nil,
    stageName: Seq[TextElement] = Nil,
    relatedContacts: Seq[Entity] = Nil,
    entityId: Option[java.net.URI] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends ContactDetails with DescriptionTypeGroup[DetailedNameContactDetails] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
    
}

case class OrganisationDetails(
    organisationName: Seq[TextElement] = Nil,
    organisationCode: Seq[java.net.URI] = Nil,
    organisationDepartment: Option[Department] = None,
    details: Seq[Details] = Nil,
    contacts: Seq[Entity] = Nil,
    organisationId: Option[java.net.URI] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[OrganisationDetails] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)  
}
case class Department(
    value: String,
    lang: Option[String] = None,
    departmentId: Option[java.net.URI]) extends TextElementType

case class Role(
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[Role] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class Details(
    emailAddress: Seq[String] = Nil,
    webAddress: Option[String] = None,
    address: Option[Address] = None,
    telephoneNumber: Option[String] = None,
    mobileTelephoneNumber: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[Details] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class Address(
    addressLine: Seq[TextElement] = Nil,
    addressTownCity: Option[TextElement] = None,
    addressCountyState: Option[TextElement] = None,
    addressDeliveryCode: Option[String] = None,
    country: Option[Country])
    
case class Country(
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[Country] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class Rights(
    rightsExpression: Seq[TextElement] = Nil,
    rightsLink: Option[java.net.URI] = None,
    rightsHolder: Option[Entity] = None,
    exploitationIssues: Option[TextElement] = None,
    coverage: Option[Coverage] = None,
    rightsClearanceFlag: Option[Boolean] = None,
    disclaimer: Seq[TextElement] = Nil,
    rightsId: Seq[Identifier] = Nil,
    contactDetails: Seq[ContactDetails] = Nil,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[Rights] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)  
}

case class Coverage(
    temporal: Option[Temporal],
    spatial: Option[Spatial])
    
case class Temporal(
    value: String,
    lang: Option[String],
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[Temporal] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}

case class Spatial(
    locationName: LocationName,
    coordinates: Option[Coordinates])

case class LocationName(
    value: String,
    lang: Option[String],
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[LocationName] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)
}
    
case class Coordinates(
    posx: Double,
    posy: Double,
    formatLabel: Option[String] = None,
    formatDefinition: Option[String] = None,
    formatLink: Option[java.net.URI] = None,
    formatLanguage: Option[String] = None) extends DescriptionFormatGroup[Coordinates] {
  override def setFormatParameters(formatLabel: Option[String], formatDefinition: Option[String], formatLink: Option[java.net.URI], formatLanguage: Option[String]) =
    copy(formatLabel = formatLabel, formatDefinition = formatDefinition, formatLink = formatLink, formatLanguage = formatLanguage)
}

case class Date(
    created: Option[DateGroup],
    modified: Option[DateGroup],
    issued: Option[DateGroup],
    alternative: Option[DateGroup]) {
  require(noTypeGroup(created))
  require(noTypeGroup(modified))
  require(noTypeGroup(issued))
  def noTypeGroup(dateGroup: Option[DateGroup]) = 
    dateGroup.map{x => x.typeLabel.isEmpty && x.typeDefinition.isEmpty && x.typeLink.isEmpty && x.typeLanguage.isEmpty}.getOrElse(true)
}
    
case class DateGroup(
    start: Long,
    end: Long,
    period: String,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[DateGroup] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)  
}
    
//      <attribute name="startYear" type="gYear"/>
//		<attribute name="startDate" type="date"/>
//		<attribute name="startTime" type="time"/>
//		<attribute name="endYear" type="gYear"/>
//		<attribute name="endDate" type="date"/>
//		<attribute name="endTime" type="time"/>
//		<attribute name="period" type="string"/>

case class Genre(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[Genre] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)    
}

case class ObjectType(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[ObjectType] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)    
}

case class TargetAudience(
    value: String,
    lang: Option[String] = None,
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends TextElementType with DescriptionTypeGroup[TargetAudience] {
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)    
}

case class Type(
    genre: Seq[Genre] = Nil,
    objectType: Seq[ObjectType] = Nil,
    targetAudience: Seq[TargetAudience] = Nil)

case class Language(
    lang: Option[String],
    typeLabel: Option[String] = None,
    typeDefinition: Option[String] = None,
    typeLink: Option[java.net.URI] = None,
    typeLanguage: Option[String] = None) extends DescriptionTypeGroup[Language] {
  require(lang.map{_.matches("""[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*""")}.getOrElse(true))
  override def setTypeParameters(typeLabel: Option[String], typeDefinition: Option[String], typeLink: Option[java.net.URI], typeLanguage: Option[String]) =
    copy(typeLabel = typeLabel, typeDefinition = typeDefinition, typeLink = typeLink, typeLanguage = typeLanguage)    

}


