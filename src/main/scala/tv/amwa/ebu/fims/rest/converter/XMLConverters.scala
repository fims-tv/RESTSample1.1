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

import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import scala.xml.NodeSeq.Empty
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Elem
import scala.xml.NamespaceBinding
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.TopScope
import scala.xml.XML
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.{Duration=>JodaDuration}
import tv.amwa.ebu.fims.rest.commons.IOUtil
import tv.amwa.ebu.fims.rest.model._
import tv.amwa.ebu.fims.rest.model.capture._
import tv.amwa.ebu.fims.rest.model.transfer._
import tv.amwa.ebu.fims.rest.model.transform._
import tv.amwa.ebu.fims.rest.model.repository._
import tv.amwa.ebu.fims.rest.Namespaces

object XMLDateTimeConverter {
  val formatter = ISODateTimeFormat.dateTime
  def toString(time: Long): String = formatter.print(time)
  def fromString(s : String) : Long = formatter.parseMillis(s)
}

object XMLDurationConverter {
  val formatter = ISOPeriodFormat.standard
  def toString(duration: Long): String = formatter.print(JodaDuration.millis(duration).toPeriod)
  def fromString(s: String): Long = formatter.parsePeriod(s).toStandardDuration.getMillis
}

object DecimalConverter {
  val formatter = new java.text.DecimalFormat("#.######")
  def toString(value: Double): String = formatter.format(value)
  def fromString(s: String): Double = s.toDouble
}

object XMLConverters {

  val NoneString = "None"
  val NoneText = scala.xml.Text(NoneString)
  def extractType(nodes: NodeSeq) = 
    nodes.headOption.map{x => x.attribute(Namespaces.xsi.uri, "type").getOrElse(NoneText).text}.getOrElse(NoneString)
    
  def writeTypeGroup(elem: scala.xml.Elem, typeGroup: TypeGroup[_]): scala.xml.Elem = 
        elem % typeGroup.typeDefinition.map{x => scala.xml.Attribute("typeDefinition", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
    	  typeGroup.typeLabel.map{x => scala.xml.Attribute("typeLabel", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
    	  typeGroup.typeLink.map{x => scala.xml.Attribute("typeLink", Text(x.toString), scala.xml.Null)}.getOrElse(scala.xml.Null)
  def writeDescriptionTypeGroup(elem: scala.xml.Elem, typeGroup: DescriptionTypeGroup[_]): scala.xml.Elem =
    writeTypeGroup(elem, typeGroup) % 
      typeGroup.typeLanguage.map{x => scala.xml.Attribute("typeLanguage", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null)
  def writeFormatGroup(elem: scala.xml.Elem, formatGroup: FormatGroup[_]): scala.xml.Elem =
        elem % formatGroup.formatDefinition.map{x => scala.xml.Attribute("formatDefinition", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
    	  formatGroup.formatLabel.map{x => scala.xml.Attribute("formatLabel", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
    	  formatGroup.formatLink.map{x => scala.xml.Attribute("formatLink", Text(x.toString), scala.xml.Null)}.getOrElse(scala.xml.Null)
  def writeDescriptionFormatGroup(elem:scala.xml.Elem, formatGroup: DescriptionFormatGroup[_]): scala.xml.Elem =
    writeFormatGroup(elem: scala.xml.Elem, formatGroup) % 
      formatGroup.formatLanguage.map{x => scala.xml.Attribute("formatLanguage", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null)
  def readTypeGroup[T <: TypeGroup[T]](nodes: NodeSeq, typeGroup: T): T =
    typeGroup.setTypeParameters(nodes \ "@typeLabel" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@typeDefinition" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@typeLink" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim))})
  def readDescriptionTypeGroup[T <: DescriptionTypeGroup[T]](nodes: NodeSeq, typeGroup: T): T =
    typeGroup.setTypeParameters(nodes \ "@typeLabel" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@typeDefinition" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@typeLink" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim))},
        nodes \ "@typeLanguage" match { case Empty => None; case xml => Some(xml.text.trim)})
  def readFormatGroup[T <: FormatGroup[T]](nodes: NodeSeq, formatGroup: T): T =
     formatGroup.setFormatParameters(nodes \ "@formatLabel" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@formatDefinition" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@formatLink" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim))})
  def readDescriptionFormatGroup[T <: DescriptionFormatGroup[T]](nodes: NodeSeq, formatGroup: T): T =
     formatGroup.setFormatParameters(nodes \ "@formatLabel" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@formatDefinition" match { case Empty => None; case xml => Some(xml.text.trim)},
        nodes \ "@formatLink" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim))},
        nodes \ "@formatLanguage" match { case Empty => None; case xml => Some(xml.text.trim)})
  def writeProfileParameters(elem: scala.xml.Elem, parameters: BaseProfileParameters): scala.xml.Elem =
    elem % parameters.name.map{x => scala.xml.Attribute("name", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
      parameters.description.map{x => scala.xml.Attribute("description", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null)
   
  def writeLengthUnit(elem: scala.xml.Elem, length: Length): scala.xml.Elem =
    elem % length.unit.map{x => scala.xml.Attribute("unit", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null)
  def readLengthUnit(nodes: NodeSeq, length: Length): Length =
    nodes \ "@unit" match { case Empty => length.copy(unit = None); case xml => length.copy(unit = Some(xml.text.trim)) }
  def writeRationalAttributes(elem: scala.xml.Elem, rational: Rational): scala.xml.Elem =
    elem % scala.xml.Attribute("numerator", Text(rational.numerator.toString), scala.xml.Attribute("denominator", Text(rational.denominator.toString), scala.xml.Null))
  def readRationalAttributes(nodes: NodeSeq, rational: Rational): Rational =
    rational.copy(numerator = (nodes \ "@numerator").text.trim.toInt, denominator = (nodes \ "@denominator").text.trim.toInt)
  def writeBMTrack(elem: scala.xml.Elem, track: BMTrack): scala.xml.Elem = {
    import StringConverters.ResourceIDConverter
    writeTypeGroup(
        elem % track.trackName.map{x => scala.xml.Attribute("trackName", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null) %
          track.trackID.map{x => scala.xml.Attribute("trackID", Text(FimsString.write(x)), scala.xml.Null)}.getOrElse(scala.xml.Null) %
          track.language.map{x => scala.xml.Attribute("language", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null), track) }
  def readBMTrack(nodes: NodeSeq, track: BMTrack): BMTrack = {
    import StringConverters.ResourceIDConverter
    readTypeGroup(nodes, track).copy(
        trackName = nodes \ "@trackName" match { case Empty => None; case xml => Some(xml.text.trim) },
        trackID = nodes \ "@trackID" match { case Empty => None; case xml => Some(FimsString.read[ResourceID](xml.text.trim)) },
        language = nodes \ "@language" match { case Empty => None; case xml => Some(xml.text.trim)} ) }
    
  implicit object EditUnitNumberConverter extends Converter[Option[EditUnitNumber],NodeSeq] {
    def write(option: Option[EditUnitNumber]): NodeSeq =
      option match {
	    case None => Empty
	    case Some(editUnit) =>
		  <tim:editUnitNumber editRate={ editUnit.editRate.toString }
		    factorNumerator={ editUnit.factorNumerator.toString } factorDenominator={ editUnit.factorDenominator.toString }>
				{ editUnit.editUnit }
		  </tim:editUnitNumber>
       }

    def read(nodes: NodeSeq): Option[EditUnitNumber] = {
      nodes match {
        case Empty => None
        case xml =>
          val editUnit = ((xml text) trim) toLong 
          val editRate = ((xml \ "@editRate" text) trim) toInt
          val factorNumerator = ((xml \ "@factorNumerator" text) trim) toInt
          val factorDenominator = ((xml \ "@factorDenominator" text) trim) toInt
          
          Some(EditUnitNumber(editUnit, editRate, factorNumerator, factorDenominator))
      }
    }
  }
  
  implicit object TimecodeConverter extends Converter[Option[Timecode],NodeSeq] {
    def write(option: Option[Timecode]): NodeSeq = {
      option match {
        case None => Empty
        case Some(timecode) =>
          <tim:timecode>{Text(timecode.timecode)}</tim:timecode>
      }
    }
    def read(nodes: NodeSeq): Option[Timecode] = {
      nodes match {
        case Empty => None
        case xml =>
          Some(Timecode((xml text) trim))
      }
    }
  }
  
  implicit object NormalPlaytimeConverter extends Converter[Option[NormalPlayTime], NodeSeq] {
    def write(option: Option[NormalPlayTime]) = {
      option match {
        case None => Empty
        case Some(npt) =>
          <tim:normalPlayTime>{Text(XMLDurationConverter.toString(npt.duration))}</tim:normalPlayTime>
      }
    }
    def read(nodes: NodeSeq): Option[NormalPlayTime] = {
      nodes match {
        case Empty => None
        case xml =>
          Some(NormalPlayTime(XMLDurationConverter.fromString((xml text) trim)))
      }
    }
  }
  
  implicit object TimeConverter extends Converter[Option[Time], NodeSeq] {
    def write(option: Option[Time]) = {
      option match {
        case None => Empty
        case Some(timeValue) => 
          timeValue.specifier match {
            case npt : NormalPlayTime => implicitly[Writer[Option[NormalPlayTime],NodeSeq]].write(Some(npt))
            case tc : Timecode => implicitly[Writer[Option[Timecode],NodeSeq]].write(Some(tc))
            case eun: EditUnitNumber => implicitly[Writer[Option[EditUnitNumber],NodeSeq]].write(Some(eun))
          }
        }
    }
    def read(nodes: NodeSeq): Option[Time] = {
      val timeChoice: Option[TimeSpecifier] = nodes.head match {
        case <timecode>{_}</timecode> => implicitly[Reader[Option[Timecode],NodeSeq]].read(nodes)
        case <normalPlayTime>{_}</normalPlayTime> => implicitly[Reader[Option[NormalPlayTime],NodeSeq]].read(nodes)
        case <editUnitNumber>{_}</editUnitNumber> => implicitly[Reader[Option[EditUnitNumber],NodeSeq]].read(nodes)
        case _ => None
      }
      timeChoice.map(Time(_))
    }
  }
  
  implicit object DurationConverter extends Converter[Option[Duration], NodeSeq] {
    def write(option: Option[Duration]) = {
      option match {
        case None => Empty
        case Some(timeValue) => 
          timeValue.specifier match {
            case npt : NormalPlayTime => implicitly[Writer[Option[NormalPlayTime],NodeSeq]].write(Some(npt))
            case tc : Timecode => implicitly[Writer[Option[Timecode],NodeSeq]].write(Some(tc))
            case eun: EditUnitNumber => implicitly[Writer[Option[EditUnitNumber],NodeSeq]].write(Some(eun))
          }
        }
    }
    def read(nodes: NodeSeq): Option[Duration] = {
      val timeChoice: Option[TimeSpecifier] = nodes.head match {
        case <timecode>{_}</timecode> => implicitly[Reader[Option[Timecode],NodeSeq]].read(nodes)
        case <normalPlayTime>{_}</normalPlayTime> => implicitly[Reader[Option[NormalPlayTime],NodeSeq]].read(nodes)
        case <editUnitNumber>{_}</editUnitNumber> => implicitly[Reader[Option[EditUnitNumber],NodeSeq]].read(nodes)
        case _ => None
      }
      timeChoice.map(Duration(_))
    }
  }

  implicit object AsyncEndpointConverter extends Converter[AsyncEndpoint, NodeSeq] {
    def write(endpoints: AsyncEndpoint) = {
      <bms:replyTo>{Text(endpoints.replyTo.toString)}</bms:replyTo> ++
      <bms:faultTo>{Text(endpoints.faultTo.toString)}</bms:faultTo>
    }
    def read(nodes: NodeSeq) = {
      AsyncEndpoint(
          new java.net.URI((nodes \ "replyTo" text).trim),
          new java.net.URI((nodes \ "faultTo" text).trim) )
    }
  }
  
  implicit object ResourceParametersConverter extends Converter[ResourceParameters, NodeSeq] {
    import StringConverters.ResourceIDConverter
    def write(parameters: ResourceParameters) = {
      <bms:resourceID>{Text(FimsString.write(parameters.resourceID))}</bms:resourceID> ++
      parameters.revisionID.map{x => <bms:revisionID>{Text(x)}</bms:revisionID>}.getOrElse(Empty) ++
      parameters.location.map{x => <bms:location>{Text(x.toString())}</bms:location>}.getOrElse(Empty) ++
      parameters.resourceCreationDate.map{x => <bms:resourceCreationDate>{Text(XMLDateTimeConverter.toString(x))}</bms:resourceCreationDate>}.getOrElse(Empty) ++
      parameters.resourceModifiedDate.map{x => <bms:resourceModifiedDate>{Text(XMLDateTimeConverter.toString(x))}</bms:resourceModifiedDate>}.getOrElse(Empty) ++
      parameters.notifyAt.map{x => <bms:notifyAt>{implicitly[Writer[AsyncEndpoint,NodeSeq]].write(x)}</bms:notifyAt>}.getOrElse(Empty) ++
      parameters.serviceGeneratedElement.map{x => <bms:serviceGeneratedElement>{Text(x.toString)}</bms:serviceGeneratedElement>}.getOrElse(Empty) ++
      parameters.isFullyPopulated.map{x => <bms:isFullyPopulated>{Text(x.toString)}</bms:isFullyPopulated>}.getOrElse(Empty) ++
      parameters.extensionGroup.map{x => <bms:ExtensionGroup>{x}</bms:ExtensionGroup>}.getOrElse(Empty) ++
      parameters.extensionAttributes.map{x => <bms:ExtensionAttributes/> % x}.getOrElse(Empty)
    }
    
    def read(nodes: NodeSeq) : ResourceParameters = {
      ResourceParameters(
          resourceID = FimsString.read(nodes \ "resourceID" text),
          revisionID = nodes \ "revisionID" match { case Empty => None; case xml => Some(xml.text.trim) },
          location = nodes \ "location" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim)) },
          resourceCreationDate = nodes \ "resourceCreationDate" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim)) },
          resourceModifiedDate = nodes \ "resourceModifiedDate" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim)) },
          notifyAt = nodes \ "notifyAt" match { case Empty => None; case xml => Some(implicitly[Reader[AsyncEndpoint,NodeSeq]].read(xml)) },
          serviceGeneratedElement = nodes \ "serviceGeneratedElement" match { case Empty => None; case xml => Some(xml.text.trim.toBoolean) },
          isFullyPopulated = nodes \ "isFullyPopulated" match { case Empty => None; case xml => Some(xml.text.trim.toBoolean) },
          extensionGroup = nodes \ "ExtensionGroup" match { case Empty => None; case xml => Some(xml.head.child) },
          extensionAttributes = nodes \ "extensionAttributes" match { case Empty => None; case xml => Some(xml.head.attributes) }
      )
    }
  }
  
  implicit object ProcessedInfoConverter extends Converter[Option[ProcessedInfoType], NodeSeq] {
    def write(info: Option[ProcessedInfoType]) = {
      info match {
        case Some(processInfo) =>
          <bms:percentageProcessedCompleted>{Text(processInfo.percentageProcessedCompleted.toString)}</bms:percentageProcessedCompleted> ++
          (processInfo match {
            case byBytes : ProcessedInfoByBytes => <bms:processedBytesCount>{Text(byBytes.processedBytesCount.toString)}</bms:processedBytesCount>
            case byFrames : ProcessedInfoByFrames => <bms:processedFramesCount>{Text(byFrames.processedFramesCount.toString)}</bms:processedFramesCount>
          })
        case None => Empty
      }
    }
    def read(nodes: NodeSeq) = {
      nodes match {
        case Empty => None
        case xml => 
          val percentageProcessedCompleted = (nodes \ "percentageProcessedCompleted" text).trim.toInt
          nodes.head.child(1) match {
            case <processedBytesCount>{bytes}</processedBytesCount> => 
              Some(ProcessedInfoByBytes(percentageProcessedCompleted, bytes.text.trim.toLong))
            case <processedFramesCount>{frames}</processedFramesCount> => 
              Some(ProcessedInfoByFrames(percentageProcessedCompleted, frames.text.trim.toLong))
            case _ => None
          }
      }
    }
  }
  
  implicit object StartJobConverter extends Converter[StartJobType, NodeSeq] {
    def write(startJob: StartJobType) = {
      startJob match {
        case noWait : StartJobByNoWait => Empty
        case time : StartJobByTime => <bms:time>{Text(XMLDateTimeConverter.toString(time.time))}</bms:time>
        case latest : StartJobByLatest => Empty
      }
    }
    def read(nodes: NodeSeq) = {
      extractType(nodes) match {
        case "bms:StartJobByNoWaitType" => StartJobByNoWait()
        case "bms:StartJobByTimeType" => nodes \ "time" match { 
          case Empty => throw new IllegalArgumentException("Start job type was not recognised.") 
          case xml => StartJobByTime(XMLDateTimeConverter.fromString(xml.text.trim))
        }
        case "bms:StartJobByLatestType" => StartJobByLatest()
        case NoneString => throw new IllegalArgumentException("Start job type was not recognised.")
      }
    }
  }
  
  implicit object StartProcessOptionConverter extends Converter[Option[StartProcessType], NodeSeq] {
    def write(startProcess: Option[StartProcessType]) = {
      startProcess match {
        case Some(start) => (start: @unchecked) match {
          case withInfo : StartProcessInfo => 
            withInfo.actualStartTime.map{x => <bms:actualStartTime>{Text(XMLDateTimeConverter.toString(x))}</bms:actualStartTime>}.getOrElse(Empty) ++
            withInfo.actualStartMark.map{x => <bms:actualStartMark>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(x))}</bms:actualStartMark>}.getOrElse(Empty)
          case byTimeMark : StartProcessByTimeMark => 
            <bms:timeMark>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(byTimeMark.timeMark))}</bms:timeMark>
          case byTime : StartProcessByTime => 
            <bms:time>{Text(XMLDateTimeConverter.toString(byTime.time))}</bms:time>
        }
        case None => Empty
      }
    }
    def read(nodes: NodeSeq) = {
      extractType(nodes) match {
        case "bms:StartProcessByNoWaitType" => Some(StartProcessByNoWait(
            actualStartTime = nodes \ "actualStartTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim))},
            actualStartMark = nodes \ "actualStartMark" match { case Empty => None; case xml => implicitly[Reader[Option[Time],NodeSeq]].read(xml)}))
        case "bms:StartProcessByServiceDefinedTimeType" => Some(StartProcessByServiceDefinedTime(
            actualStartTime = nodes \ "actualStartTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim))},
            actualStartMark = nodes \ "actualStartMark" match { case Empty => None; case xml => implicitly[Reader[Option[Time],NodeSeq]].read(xml)}))
        case "bms:StartProcessByTimeMarkType" => 
          nodes \ "timeMark" match {
            case Empty => throw new IllegalArgumentException("Invalid time specification for start process by time mark type.")
            case xml => Some(StartProcessByTimeMark(implicitly[Reader[Option[Time],NodeSeq]].read(xml.head.child).get))
          }
        case "bms:StartProcessByTimeType" =>
          Some(StartProcessByTime(XMLDateTimeConverter.fromString((nodes \ "time").text.trim)))
        case NoneString => None
      }
    }
  }
  
  implicit object StartProcessConverter extends Converter[StartProcessType, NodeSeq] {
    def write(startProcess: StartProcessType) = implicitly[Writer[Option[StartProcessType], NodeSeq]].write(Some(startProcess))
    def read(nodes: NodeSeq) = implicitly[Reader[Option[StartProcessType], NodeSeq]].read(nodes).get
  }
  
  implicit object StopProcessOptionConverter extends Converter[Option[StopProcessType], NodeSeq] {
    def write(stopProcess: Option[StopProcessType]) = {
      stopProcess match {
        case Some(stop) => (stop: @unchecked) match {
          case withInfo : StopProcessInfo =>
            withInfo.actualStopTime.map{x => <bms:actualStopTime>{Text(XMLDateTimeConverter.toString(x))}</bms:actualStopTime>}.getOrElse(Empty) ++
            withInfo.actualStopMark.map{x => <bms:actualStopMark>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(x))}</bms:actualStopMark>}.getOrElse(Empty)
          case byTimeMark : StopProcessByTimeMark =>
            <bms:timeMark>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(byTimeMark.timeMark))}</bms:timeMark>
          case byTime : StopProcessByTime =>
            <bms:time>{Text(XMLDateTimeConverter.toString(byTime.time))}</bms:time>
          case byDuration : StopProcessByDuration =>
            <bms:duration>{implicitly[Writer[Option[Duration],NodeSeq]].write(Some(byDuration.duration))}</bms:duration>
        }
        case None => Empty
      }
    }
    def read(nodes: NodeSeq) = {
      extractType(nodes) match {
        case "bms:StopProcessByOpenEndType" => Some(StopProcessByOpenEnd(
            actualStopTime = nodes \ "actualStopTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim))},
            actualStopMark = nodes \ "actualStopMark" match { case Empty => None; case xml => implicitly[Reader[Option[Time],NodeSeq]].read(xml)})) 
        case "bms:StopProcessByServiceDefinedTimeType" => Some(StopProcessByServiceDefinedTime(
            actualStopTime = nodes \ "actualStopTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim))},
            actualStopMark = nodes \ "actialStopMark" match { case Empty => None; case xml => implicitly[Reader[Option[Time],NodeSeq]].read(xml)})) 
        case "bms:StopProcessByTimeMarkType" => nodes \ "timeMark" match {
          case Empty => throw new IllegalArgumentException("Invalid time specification for stop process by time type.")
          case xml => Some(StopProcessByTimeMark(implicitly[Reader[Option[Time],NodeSeq]].read(xml.head.child).get))
        }
        case "bms:StopProcessByTimeType" => nodes \ "time" match {
          case Empty => throw new IllegalArgumentException("Invalid time specification for stop process by time type.")
          case xml => Some(StopProcessByTime(XMLDateTimeConverter.fromString(xml.text.trim)))
        }
        case "bms:StopProcessByDurationType" => nodes \ "duration" match {
          case Empty => throw new IllegalArgumentException("Invalid duration specification for stop process by duration type.")
          case xml => Some(StopProcessByDuration(implicitly[Reader[Option[Duration],NodeSeq]].read(xml.head.child).get))
        }
        case NoneString => None
      }
    }
  }
  
  implicit object StopProcessConverter extends Converter[StopProcessType, NodeSeq] {
    def write(stopProcess: StopProcessType) = implicitly[Writer[Option[StopProcessType], NodeSeq]].write(Some(stopProcess))
    def read(nodes: NodeSeq) = implicitly[Reader[Option[StopProcessType], NodeSeq]].read(nodes).get
  }
  
  implicit object SourceInPointConverter extends Converter[Option[SourceInPointType], NodeSeq] {
    def write(inPoint: Option[SourceInPointType]) = {
      inPoint.getOrElse(()) match {
        case byTime : SourceInPointByTimeMark => <cms:timeMark>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(byTime.timeMark))}</cms:timeMark>
        case byCurrent : SourceInPointByCurrent => Empty
        case byBeginning : SourceInPointByBeginning => Empty
        case missing : Unit => Empty 
      }
    }
    def read(nodes: NodeSeq) = {
      extractType(nodes) match {
        case "cms:SourceInPointByTimeMarkType" => Some(SourceInPointByTimeMark(implicitly[Reader[Option[Time],NodeSeq]].read(nodes \ "timeMark").get))
        case "cms:SourceInPointByCurrentType" => Some(SourceInPointByCurrent())
        case "cms:SourceInPointByBeginningType" => Some(SourceInPointByBeginning())
        case NoneString => None 
      }
    }
  }
  
  implicit object SourceOutPointConverter extends Converter[Option[SourceOutPointType], NodeSeq] {
    def write(outPoint: Option[SourceOutPointType]) = {
      outPoint.getOrElse(()) match {
        case byDuration : SourceOutPointByDuration => 
          <duration>{implicitly[Writer[Option[Duration], NodeSeq]].write(Some(byDuration.duration))}</duration>
        case byTime : SourceOutPointByTimeMark => 
          <time>{implicitly[Writer[Option[Time],NodeSeq]].write(Some(byTime.timeMark))}</time>
        case byEnd : SourceOutPointByEnd => Empty
        case byOpenEndType : SourceOutPointByOpenEnd => Empty
        case nothing : Unit => Empty
      }
    }
    def read(nodes: NodeSeq) = {
      extractType(nodes) match {
        case "cms:SourceOutPointByDurationType" => 
          Some(SourceOutPointByDuration(implicitly[Reader[Option[Duration],NodeSeq]].read((nodes \ "duration").head.child).get))
        case "cms:SourceOutPointByTimeMarkType" =>
          Some(SourceOutPointByTimeMark(implicitly[Reader[Option[Time],NodeSeq]].read((nodes \ "time").head.child).get))
        case "cms:SourceOutPointByEndType" => Some(SourceOutPointByEnd())
        case "cms:SourceOutPointByOpenEnd" => Some(SourceOutPointByOpenEnd())
        case NoneString => None
      }
    }
  }
  
  implicit object CaptureProfileParametersConverter extends Converter[CaptureProfileParameters, NodeSeq] {
    def write(parameters: CaptureProfileParameters) = {
      parameters.transformAtom.map{x => <transformAtom>{TransformAtomConverter.write(x)}</transformAtom>}.getOrElse(Empty) ++
      parameters.transferAtom.map{x => <transferAtom>{TransferAtomConverter.write(x)}</transferAtom>} ++
      parameters.outputFileNamePattern.map{x => <outputFileNamePattern>{Text(x)}</outputFileNamePattern>}.getOrElse(Empty)
    }
    def read(nodes: NodeSeq) = {
      CaptureProfileParameters(
          transformAtom = nodes \ "transformAtom" match { case Empty => None; case xml => Some(implicitly[Reader[TransformAtom,NodeSeq]].read(xml)) },
          transferAtom = (nodes \ "transferAtom").map{x => implicitly[Reader[TransferAtom, NodeSeq]].read(x)},
          outputFileNamePattern = nodes \ "outputFileNamePattern" match { case Empty => None; case xml => Some(xml.text.trim) }
       )
    }
  }
    
  implicit object BaseProfileParametersConverter extends Converter[BaseProfileParameters, NodeSeq] {
    def write(parameters: BaseProfileParameters) = {
      parameters.service.map{x => implicitly[Writer[ServiceType,NodeSeq]].write(x)}.getOrElse(Empty)
    }
    def read(nodes: NodeSeq) = {
      BaseProfileParameters(
          service = nodes \ "service" match { case Empty => None; case xml => Some(implicitly[Reader[ServiceType,NodeSeq]].read(xml))},
          name = nodes \ "@name" match { case Empty => None; case xml => Some(xml.text.trim)},
          description = nodes \ "@description" match { case Empty => None; case xml => Some(xml.text.trim)}
      )
    }
  }
  
  implicit object CaptureProfileConverter extends Converter[CaptureProfileType, NodeSeq] {
    def write(profile: CaptureProfileType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(writeProfileParameters(<bms:profile xsi:type="cms:CaptureProfileType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(profile.resourceParameters) ++
        implicitly[Writer[BaseProfileParameters,NodeSeq]].write(profile.baseParameters) ++
        implicitly[Writer[CaptureProfileParameters,NodeSeq]].write(profile.serviceParameters)
      }</bms:profile>, profile.baseParameters), Namespaces.cms)
    }
    def read(nodes: NodeSeq) = {
      CaptureProfile(
          implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseProfileParameters,NodeSeq]].read(nodes),
          implicitly[Reader[CaptureProfileParameters,NodeSeq]].read(nodes)
       )
    }
  }
  
  implicit object TransformProfileParametersConverter extends Converter[TransformProfileParameters, NodeSeq] {
    def write(parameters: TransformProfileParameters) = {
      parameters.transformAtom.map{x => <transformAtom>{TransformAtomConverter.write(x)}</transformAtom>}.getOrElse(Empty) ++
      parameters.transferAtom.map{x => <transferAtom>{TransferAtomConverter.write(x)}</transferAtom>} ++
      parameters.outputFileNamePattern.map{x => <outputFileNamePattern>{Text(x)}</outputFileNamePattern>}.getOrElse(Empty)
    }
    def read(nodes: NodeSeq) = {
      TransformProfileParameters(
          transformAtom = nodes \ "transformAtom" match { case Empty => None; case xml => Some(implicitly[Reader[TransformAtom,NodeSeq]].read(xml)) },
          transferAtom = (nodes \ "transferAtom").map{x => implicitly[Reader[TransferAtom, NodeSeq]].read(x)},
          outputFileNamePattern = nodes \ "outputFileNamePattern" match { case Empty => None; case xml => Some(xml.text.trim) }
       )
    }
  }
  
  implicit object TransferProfileParametersConverter extends Converter[TransferProfileParameters, NodeSeq] {
    def write(parameters: TransferProfileParameters) = {
      parameters.transferAtom.map{x => <transferAtom>{TransferAtomConverter.write(x)}</transferAtom>}
    }
    def read(nodes: NodeSeq) = {
      TransferProfileParameters(
          transferAtom = (nodes \ "transferAtom").map{x => implicitly[Reader[TransferAtom, NodeSeq]].read(x)}
       )
    }
  }

  implicit object TransformProfileConverter extends Converter[TransformProfileType, NodeSeq] {
    def write(profile: TransformProfileType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(writeProfileParameters(<bms:profile xsi:type="tfms:TransformProfileType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(profile.resourceParameters) ++
        implicitly[Writer[BaseProfileParameters,NodeSeq]].write(profile.baseParameters) ++
        implicitly[Writer[TransformProfileParameters,NodeSeq]].write(profile.serviceParameters)
      }</bms:profile>, profile.baseParameters), Namespaces.tfms)
    }
    def read(nodes: NodeSeq) = {
      TransformProfile(
         implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseProfileParameters,NodeSeq]].read(nodes),
          implicitly[Reader[TransformProfileParameters,NodeSeq]].read(nodes)
      )
    }
  }
    
  implicit object TransferProfileConverter extends Converter[TransferProfileType, NodeSeq] {
    def write(profile: TransferProfileType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(writeProfileParameters(<bms:profile xsi:type="tms:TransferProfileType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(profile.resourceParameters) ++
        implicitly[Writer[BaseProfileParameters,NodeSeq]].write(profile.baseParameters) ++
        implicitly[Writer[TransferProfileParameters,NodeSeq]].write(profile.serviceParameters)
      }</bms:profile>, profile.baseParameters), Namespaces.tms)
    }
    def read(nodes: NodeSeq) = {
      TransferProfile(
         implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseProfileParameters,NodeSeq]].read(nodes),
          implicitly[Reader[TransferProfileParameters,NodeSeq]].read(nodes)
      )
    }
  }
  
  implicit object CaptureJobParametersConverter extends Converter[CaptureJobParameters, NodeSeq] {
    def write(parameters: CaptureJobParameters) = {
      (parameters.profiles match {
        case Seq() => Empty
        case profiles @ Seq(_*) => <profiles>{
          profiles.map{x => writeProfileParameters(<captureProfile>{implicitly[Writer[CaptureProfileType,NodeSeq]].write(x).head.child}</captureProfile>, 
              x.baseParameters)}
        }</profiles>
      }) ++
      parameters.startProcess.map{x => <startProcess xsi:type={x match {
          case sp : StartProcessByNoWait => "bms:StartProcessByNoWaitType"
          case sp : StartProcessByServiceDefinedTime => "bms:StartProcessByServiceDefinedType"
          case sp : StartProcessByTimeMark => "bms:StartProcessByTimeMarkType"
          case sp : StartProcessByTime => "bms:StartProcessByTimeType"
        }}>{implicitly[Writer[Option[StartProcessType],NodeSeq]].write(Some(x))}</startProcess>}.getOrElse(Empty) ++
      parameters.stopProcess.map{x => <stopProcess xsi:type={x match {
          case sp : StopProcessByOpenEnd => "bms:StopProcessByOpenEndType"
          case sp : StopProcessByServiceDefinedTime => "bms:StopProcessByServiceDefinedTimeType"
          case sp : StopProcessByTimeMark => "bms:StopProcessByTimeMarkType"
          case sp : StopProcessByTime => "bms:StopProcessByTimeType"
          case sp : StopProcessByDuration => "bms:StopProcessByDurationType"
        }}>{implicitly[Writer[Option[StopProcessType],NodeSeq]].write(Some(x))}</stopProcess>}.getOrElse(Empty) ++
      parameters.sourceID.map{x => <sourceID>{Text(x.toString)}</sourceID>}.getOrElse(Empty) ++ 
      parameters.sourceType.map{x => <sourceType>{Text(x.toString)}</sourceType>}.getOrElse(Empty) ++
      parameters.inPoint.map{x => <inPoint xsi:type={x match {
            case byTime : SourceInPointByTimeMark => "cms:SourceInPointByTimeMarkType" 
            case byBeginning : SourceInPointByBeginning => "cms:SourceInPointByBeginningType"
            case byCurrent : SourceInPointByCurrent => "cms:SourceInPointByCurrentType"
          }}>{implicitly[Writer[Option[SourceInPointType], NodeSeq]].write(Some(x))}</inPoint>}.getOrElse(Empty) ++
      parameters.outPoint.map{x => <outPoint xsi:type={x match {
            case byDuration : SourceOutPointByDuration => "cms:SourceOutPointByDurationType"
            case byTime : SourceOutPointByTimeMark => "cms:SourceOutPointByTimeMarkType"
            case byEnd : SourceOutPointByEnd=> "cms:SourceOutPointByEndType"
            case byOpenEnd : SourceOutPointByOpenEnd => "cms:SourceOutPointByOpenEndType"
          }}>{implicitly[Writer[Option[SourceOutPointType], NodeSeq]].write(Some(x))}</outPoint>}.getOrElse(Empty) ++
      parameters.splitOnTCBreak.map(x => <splitOnTCBreak>{Text(x.toString)}</splitOnTCBreak>).getOrElse(Empty)
    }
    def read(nodes: NodeSeq) : CaptureJobParameters = {
      CaptureJobParameters(
          profiles = nodes \ "profiles" match {
            case Empty => Nil
            case xml => xml \ "captureProfile" map {x => implicitly[Reader[CaptureProfileType,NodeSeq]].read(x)}
          },
          startProcess = implicitly[Reader[Option[StartProcessType],NodeSeq]].read(nodes \ "startProcess"),
          stopProcess = implicitly[Reader[Option[StopProcessType],NodeSeq]].read(nodes \ "stopProcess"),
          sourceID = nodes \ "sourceID" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim)) },
          sourceType = nodes \ "sourceType" match { case Empty => None; case xml => Some(SourceType.fromString(xml.text.toLowerCase.trim)) },
          inPoint = implicitly[Reader[Option[SourceInPointType], NodeSeq]].read(nodes \ "inPoint"),
          outPoint = implicitly[Reader[Option[SourceOutPointType], NodeSeq]].read(nodes \ "outPoint"),
          splitOnTCBreak = nodes \ "splitOnTCBreak" match { case Empty => None; case xml => Some(xml.text.trim.toBoolean) }
      )
    }
  }
  
  implicit object TransferJobParametersConverter extends Converter[TransferJobParameters, NodeSeq] {
    def write(parameters: TransferJobParameters) = {
      (parameters.profiles match {
        case Seq() => Empty
        case profiles @ Seq(_*) => <profiles>{
          profiles.map{x => writeProfileParameters(<transferProfile>{implicitly[Writer[TransferProfileType,NodeSeq]].write(x).head.child}</transferProfile>, 
              x.baseParameters)}
        }</profiles>
      })
    }
    def read(nodes: NodeSeq) = {
      TransferJobParameters(profiles = nodes \ "profiles" match {
            case Empty => Nil
            case xml => xml \ "transferProfile" map {x => implicitly[Reader[TransferProfileType,NodeSeq]].read(x)}
          })
    }
  }
  
  implicit object TransformJobParametersConverter extends Converter[TransformJobParameters, NodeSeq] {
    def write(parameters: TransformJobParameters) = {
      (parameters.profiles match {
        case Seq() => Empty
        case profiles @ Seq(_*) => <profiles>{
          profiles.map{x => writeProfileParameters(<transformProfile>{implicitly[Writer[TransformProfileType,NodeSeq]].write(x).head.child}</transformProfile>, 
              x.baseParameters)}
        }</profiles>
      })
    }
    def read(nodes: NodeSeq) = {
       TransformJobParameters(profiles = nodes \ "profiles" match {
            case Empty => Nil
            case xml => xml \ "transformProfile" map {x => implicitly[Reader[TransformProfileType,NodeSeq]].read(x)}
          })
    }
   }
    
  implicit object ServiceConverter extends Converter[ServiceType, NodeSeq] {
    def write(service: ServiceType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:service>{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(service.resourceParameters) ++
        service.providerName.map{x => <bms:providerName>{Text(x)}</bms:providerName>}.getOrElse(Empty) ++
        service.providerEndPoint.map{x => <bms:providerEndPoint>{Text(x.toString)}</bms:providerEndPoint>}.getOrElse(Empty) ++
        service.serviceDescription.map{x => <bms:serviceDescription>{x}</bms:serviceDescription>}.getOrElse(Empty)
      }</bms:service>,
      Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      Service(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          providerName = nodes \ "providerName" match { case Empty => None; case xml => Some(xml.text)},
          providerEndPoint = nodes \ "providerEndPoint" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim))},
          serviceDescription = nodes \ "serviceDescription" match { case Empty => None; case xml => Some(implicitly[NodeSeq](xml.head.child)) }
      )
    }
  }
    
  implicit object TechnicalAttributeConverter extends Converter[TechnicalAttribute, NodeSeq] {
    def write(technical: TechnicalAttribute) = {
      writeTypeGroup(writeFormatGroup(<bms:technicalAttribute>{Text(technical.value)}</bms:technicalAttribute>, technical), technical)
    }
    def read(nodes: NodeSeq) = {
      readTypeGroup(nodes, readFormatGroup(nodes, TechnicalAttribute((nodes \\ "technicalAttribute").text.trim)))
    }
  }
  
  implicit object CodecConverter extends Converter[Codec,NodeSeq] {
    def write(codec: Codec) = {
      codec.name.map{x => <bms:name>{Text(x)}</bms:name>}.getOrElse(Empty) ++
      codec.vendor.map{x => <bms:vendor>{Text(x)}</bms:vendor>}.getOrElse(Empty) ++
      codec.version.map{x => <bms:version>{Text(x)}</bms:version>}.getOrElse(Empty) ++
      codec.family.map{x => <bms:family>{Text(x)}</bms:family>}.getOrElse(Empty)
    }
    def read(nodes : NodeSeq) = {
      readTypeGroup(nodes, Codec(
          name = nodes \ "name" match { case Empty => None; case xml => Some(xml.text.trim)},
          vendor = nodes \ "vendor" match { case Empty => None; case xml => Some(xml.text.trim)},
          version = nodes \ "version" match { case Empty => None; case xml => Some(xml.text.trim)},
          family = nodes \ "family" match { case Empty => None; case xml => Some(xml.text.trim)} ))
    }
  }
  
  implicit object VideoFormatConverter extends Converter[VideoFormatType, NodeSeq] {
    def write(format: VideoFormatType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:videoFormat>{
	      implicitly[Writer[ResourceParameters,NodeSeq]].write(format.resourceParameters) ++
	      format.technicalAttribute.flatMap{x => implicitly[Writer[TechnicalAttribute,NodeSeq]].write(x)} ++
	      format.displayWidth.map{x => writeLengthUnit(<bms:displayWidth>{Text(x.value.toString)}</bms:displayWidth>, x)}.getOrElse(Empty) ++
	      format.displayHeight.map{x => writeLengthUnit(<bms:displayHeight>{Text(x.value.toString)}</bms:displayHeight>, x)}.getOrElse(Empty) ++
	      format.frameRate.map{x => writeRationalAttributes(<bms:frameRate>{Text(x.value.toString)}</bms:frameRate>, x)}.getOrElse(Empty) ++
	      format.aspectRatio.map{x => writeRationalAttributes(<bms:aspectRatio>{Text(x.value.toString)}</bms:aspectRatio>, x)}.getOrElse(Empty) ++
	      format.videoEncoding.map{x => 
	        writeTypeGroup(<bms:videoEncoding>{implicitly[Writer[Codec,NodeSeq]].write(x)}</bms:videoEncoding>, x)}.getOrElse(Empty) ++
	      format.videoTrack.flatMap{x => writeBMTrack(<bms:videoTrack>{/* TODO Extensions */}</bms:videoTrack>, x)} ++
	      format.bitRate.map{x => <bms:bitRate>{Text(x.toString)}</bms:bitRate>}.getOrElse(Empty) ++
	      format.bitRateMode.map{x => <bms:bitRateMode>{Text(x.toString)}</bms:bitRateMode>}.getOrElse(Empty) ++
	      format.lines.map{x => <bms:lines>{Text(x.toString)}</bms:lines>}.getOrElse(Empty) ++
	      format.scanningFormat.map{x => <bms:scanningFormat>{Text(x.toString)}</bms:scanningFormat>}.getOrElse(Empty) ++
	      format.scanningOrder.map{x => <bms:scanningOrder>{Text(x.toString)}</bms:scanningOrder>}.getOrElse(Empty) ++
	      format.noiseFilter.map{x => <bms:noiseFilter>{Text(x.toString)}</bms:noiseFilter>}.getOrElse(Empty)}</bms:videoFormat>,
	      Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      VideoFormat(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          technicalAttribute = nodes \ "technicalAttribute" map{x => implicitly[Reader[TechnicalAttribute,NodeSeq]].read(x)},
          displayWidth = nodes \ "displayWidth" match { case Empty => None; case xml => Some(readLengthUnit(xml, Length(xml.text.trim.toLong))) },
          displayHeight = nodes \ "displayHeight" match { case Empty => None; case xml => Some(readLengthUnit(xml, Length(xml.text.trim.toLong))) },
          frameRate = nodes \ "frameRate" match { case Empty => None; case xml => Some(readRationalAttributes(xml, Rational(xml.text.trim.toLong)))},
          aspectRatio = nodes \ "aspectRatio" match { case Empty => None; case xml => Some(readRationalAttributes(xml, Rational(xml.text.trim.toLong)))},
          videoEncoding = nodes \ "videoEncoding" match { case Empty => None; case xml => Some(implicitly[Reader[Codec,NodeSeq]].read(xml))},
          videoTrack = (nodes \ "videoTrack").map{x => readBMTrack(x, BMTrack(/* TODO extension */))},
          bitRate = nodes \ "bitRate" match { case Empty => None; case xml => Some(xml.text.trim.toLong)},
          bitRateMode = nodes \ "bitRateMode" match { case Empty => None; case xml => Some(BitRateModeType.fromString(xml.text.trim.toLowerCase))},
          lines = nodes \ "lines" match { case Empty => None; case xml => Some(xml.text.trim.toInt)},
          scanningFormat = nodes \ "scanningFormat" match { case Empty => None; case xml => Some(ScanningFormatType.fromString(xml.text.trim.toLowerCase))},
          scanningOrder = nodes \ "scanningOrder" match { case Empty => None; case xml => Some(ScanningOrderType.fromString(xml.text.trim.toLowerCase))},
          noiseFilter = nodes \ "noiseFilter" match { case Empty => None; case xml => Some(xml.text.trim.toLowerCase.toBoolean)}
      )
    }
  }
  
  implicit object AudioFormatConverter extends Converter[AudioFormatType,NodeSeq] {
    def write(format: AudioFormatType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:audioFormat>{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(format.resourceParameters) ++
    	format.technicalAttribute.flatMap{x => implicitly[Writer[TechnicalAttribute,NodeSeq]].write(x)} ++
        format.samplingRate.map{x => <bms:samplingRate>{Text(DecimalConverter.toString(x))}</bms:samplingRate>}.getOrElse(Empty) ++     
        format.audioEncoding.map{x => 
          writeTypeGroup(<bms:audioEncoding>{implicitly[Writer[Codec,NodeSeq]].write(x)}</bms:audioEncoding>, x)}.getOrElse(Empty) ++
        format.trackConfiguration.map{x => writeTypeGroup(<bms:trackConfiguration/>, x)}.getOrElse(Empty) ++
        format.audioTrack.flatMap{x => writeBMTrack(<bms:audioTrack>{/* TODO extensions */}</bms:audioTrack>, x)} ++
        format.channels.map{x => <bms:channels>{Text(x.toString)}</bms:channels>}.getOrElse(Empty) ++
	    format.bitRate.map{x => <bms:bitRate>{Text(x.toString)}</bms:bitRate>}.getOrElse(Empty) ++
	    format.bitRateMode.map{x => <bms:bitRateMode>{Text(x.toString)}</bms:bitRateMode>}.getOrElse(Empty) ++
	    format.sampleSize.map{x => <bms:sampleSize>{Text(x.toString)}</bms:sampleSize>}.getOrElse(Empty) ++
        format.sampleType.map{x => <bms:sampleType>{Text(x.toString)}</bms:sampleType>}.getOrElse(Empty)}</bms:audioFormat>,
        Namespaces.bms)
    } 
    def read(nodes: NodeSeq) = {
      AudioFormat(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          technicalAttribute = nodes \ "technicalAttribute" map{x => implicitly[Reader[TechnicalAttribute,NodeSeq]].read(x)},
          samplingRate = nodes \ "samplingRate" match { case Empty => None; case xml => Some(DecimalConverter.fromString(xml.text.trim))},
          audioEncoding = nodes \ "audioEncoding" match { case Empty => None; case xml => Some(implicitly[Reader[Codec,NodeSeq]].read(xml))},
          trackConfiguration = nodes \ "trackConfiguration" match { case Empty => None; case xml => Some(readTypeGroup(xml, TrackConfiguration())) },
          audioTrack = (nodes \ "audioTrack").map{x => readBMTrack(x, BMTrack(/* TODO extension */))},
          channels = nodes \ "channels" match { case Empty => None; case xml => Some(xml.text.trim.toInt)},
          bitRate = nodes \ "bitRate" match { case Empty => None; case xml => Some(xml.text.trim.toLong)},
          bitRateMode = nodes \ "bitRateMode" match { case Empty => None; case xml => Some(BitRateModeType.fromString(xml.text.trim.toLowerCase))},
          sampleSize = nodes \ "sampleSize" match { case Empty => None; case xml => Some(xml.text.trim.toInt)},
          sampleType = nodes \ "sampleType" match { case Empty => None; case xml => Some(AudioSampleType.fromString(xml.text.trim.toLowerCase))}
      )
    }
  }
  
  implicit object DataFormatConverter extends Converter[DataFormatType,NodeSeq] {
    def write(format: DataFormatType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:dataFormat>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(format.resourceParameters) ++
    	  format.technicalAttribute.flatMap{x => implicitly[Writer[TechnicalAttribute,NodeSeq]].write(x)} ++
          format.captioningFormat.map{x => writeFormatGroup(<bms:captioningFormat>{Text(x.value)}</bms:captioningFormat>, x) %
    	    x.captioningSourceUri.map{y => scala.xml.Attribute("captioningSourceUri", Text(y.toString), scala.xml.Null)}.getOrElse(scala.xml.Null) %
    	    x.language.map{y => scala.xml.Attribute("language", Text(y), scala.xml.Null)}.getOrElse(scala.xml.Null)} ++
          format.ancillaryDataFormat.map{x => <bms:ancillaryDataFormat>{
            x.DID.map{y => <bms:DID>{Text(y.toString)}</bms:DID>}.getOrElse(Empty) ++
            x.SDID.map{y => <bms:SDID>{Text(y.toString)}</bms:SDID>}.getOrElse(Empty) ++
            x.lineNumber.map{y => <bms:lineNumber>{Text(y.toString)}</bms:lineNumber>}.getOrElse(Empty) ++
            x.wrappingType.map{y => <bms:wrappingType>{Text(y.toString)}</bms:wrappingType>}.getOrElse(Empty)
          }</bms:ancillaryDataFormat>}}</bms:dataFormat>,
          Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      DataFormat(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          technicalAttribute = nodes \ "technicalAttribute" map {x => implicitly[Reader[TechnicalAttribute,NodeSeq]].read(x)},
          captioningFormat = nodes \ "captioningFormat" map {x => readFormatGroup(x, CaptioningFormat(
              value = x text,
              captioningSourceUri = x \ "@captioningSourceUri" match { case Empty => None; case xml => Some(new java.net.URI(xml text))},
              language = x \ "@language" match { case Empty => None; case xml => Some(xml text)}))},
          ancillaryDataFormat = nodes \ "ancillaryDataFormat" map {x => AncillaryDataFormat(
              DID = x \ "DID" match { case Empty => None; case xml => Some(xml.text.toInt)},
              SDID = x \ "SDID" match { case Empty => None; case xml => Some(xml.text.toInt)},
              lineNumber = x \ "lineNumber" match { case Empty => None; case xml => Some(xml.text.toInt)},
              wrappingType = x \ "wrappingType" match { case Empty => None; case xml => Some(xml.text.toInt)})}
      )
    }
  }
  
  implicit object ContainerFormatConverter extends Converter[ContainerFormatType,NodeSeq] {
    def write(format: ContainerFormatType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:containerFormat>{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(format.resourceParameters) ++
    	format.technicalAttribute.flatMap{x => implicitly[Writer[TechnicalAttribute,NodeSeq]].write(x)} ++
    	format.containerFormat.map{x => writeFormatGroup(<bms:containerFormat>{Text(x.value)}</bms:containerFormat>, x)}.getOrElse(Empty)}</bms:containerFormat>,
    	Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      ContainerFormat(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          technicalAttribute = nodes \ "technicalAttribute" map{x => implicitly[Reader[TechnicalAttribute,NodeSeq]].read(x)},
          containerFormat = nodes \ "containerFormat" match { case Empty => None; case xml => Some(readFormatGroup(xml, ContainerFormatParameters(xml.text.trim)))}
      )
    }
  }
  
  implicit object TransformAtomConverter extends Converter[TransformAtom, NodeSeq] {
    def write(atom: TransformAtom) = {
       atom.videoFormat.map{x => implicitly[Writer[VideoFormatType,NodeSeq]].write(x)}.getOrElse(Empty) ++
       atom.audioFormat.map{x => implicitly[Writer[AudioFormatType,NodeSeq]].write(x)}.getOrElse(Empty) ++
       atom.containerFormat.map{x => implicitly[Writer[ContainerFormatType,NodeSeq]].write(x)}.getOrElse(Empty)
    }
    def read(nodes: NodeSeq) = {
      TransformAtom(
          videoFormat = nodes \ "videoFormat" match { case Empty => None; case xml => Some(implicitly[Reader[VideoFormatType,NodeSeq]].read(xml))},
          audioFormat = nodes \ "audioFormat" match { case Empty => None; case xml => Some(implicitly[Reader[AudioFormatType,NodeSeq]].read(xml))},
          containerFormat = nodes \ "containerFormat" match { case Empty => None; case xml => Some(implicitly[Reader[ContainerFormatType,NodeSeq]].read(xml))}
      )
    }
  }
  
  implicit object TransferAtomConverter extends Converter[TransferAtom, NodeSeq] {
    def write(atom: TransferAtom) = {
      <bms:destination>{Text(atom.destination.toString)}</bms:destination>
    }
    def read(nodes: NodeSeq) = {
	   TransferAtom(new java.net.URI((nodes \ "destination").text.trim))
    }
  }
  
  implicit object BMEssenceLocatorConverter extends Converter[BMEssenceLocatorType,NodeSeq] {
    def write(locator: BMEssenceLocatorType) = {
       XMLNamespaceProcessor.setNameSpaceIfAbsent(
           <bms:bmEssenceLocator xsi:type={Text(locator match {
             case l : SimpleFileLocatorType => "bms:SimpleFileLocatorType"
             case l : ListFileLocatorType => "bms:ListFileLocatorType"
             case l : FolderLocatorType => "bms:FolderLocatorType"
           })}>{
             implicitly[Writer[ResourceParameters,NodeSeq]].write(locator.resourceParameters) ++
             locator.storageType.map{x => writeTypeGroup(<bms:storageType>{Text(x.value.toString)}</bms:storageType>, x)}.getOrElse(Empty) ++
             locator.locatorInfo.map{x => <bms:locatorInfo>{Text(x.toString)}</bms:locatorInfo>}.getOrElse(Empty) ++
             locator.containerMimeType.map{x => writeTypeGroup(<bms:containerMimeType>{Text(x.value)}</bms:containerMimeType>, x)}.getOrElse(Empty) ++
             (locator match {
               case sfl : SimpleFileLocatorType => sfl.file.map{x => <bms:file>{Text(x.toString)}</bms:file>}.getOrElse(Empty)
               case lfl : ListFileLocatorType => lfl.file match { 
                 case Seq() => Empty 
                 case files @ Seq(_*) => files.map{x => <bms:file>{Text(x.toString)}</bms:file>}
               }
               case fl : FolderLocatorType => fl.folder.map{x => <bms:folder>{Text(x.toString)}</bms:folder>}.getOrElse(Empty)
             })
           }</bms:bmEssenceLocator>,
           Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      val resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes)
      val storageType = nodes \ "storageType" match { case Empty => None; case xml => Some(readTypeGroup(xml, Storage(StorageTypes.fromString(xml.text.toLowerCase)))) }
      val locatorInfo = nodes \ "locatorInfo" match { case Empty => None; case xml => Some(xml.text)}
      val containerMimeType = nodes \ "containerMimeType" match { case Empty => None; case xml => Some(readTypeGroup(xml, ContainerMimeType(xml.text))) }
      val status = nodes \ "status" match { case Empty => None; case _ => None } // FIXME parse properly
      extractType(nodes) match {
        case "bms:SimpleFileLocatorType" => SimpleFileLocator(resourceParameters, storageType, locatorInfo, containerMimeType, status,
            nodes \ "file" match { case Empty => None; case xml => Some(new java.net.URI(xml.text))})
        case "bms:ListFileLocatorType" => ListFileLocator(resourceParameters, storageType, locatorInfo, containerMimeType, status,
            nodes \ "file" match {
              case Seq() => Nil
              case files @ Seq(_*) => files.map{x => new java.net.URI(x.text)}
        })
        case "bms:FolderLocatorType" => FolderLocator(resourceParameters, storageType, locatorInfo, containerMimeType, status,
             nodes \ "folder" match { case Empty => None; case xml => Some(new java.net.URI(xml.text))})  
        case NoneString => throw new IllegalArgumentException("Failed to match the type of an essence locator.")
      }
    }
  }

  implicit object BMContentFormatConverter extends Converter[BMContentFormatType,NodeSeq] {
    def write(contentFormat: BMContentFormatType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:bmContentFormat>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(contentFormat.resourceParameters) ++
          (contentFormat.bmEssenceLocators match { 
            case Seq() => Empty
            case locators @ Seq(_*) => <bms:bmEssenceLocators>{
              locators.map{x => implicitly[Writer[BMEssenceLocatorType,NodeSeq]].write(x)}
            }</bms:bmEssenceLocators>
          }) ++
          contentFormat.formatCollection.map{x => <bms:formatCollection>{
              x.videoFormat.map{y => implicitly[Writer[VideoFormatType,NodeSeq]].write(y)}.getOrElse(Empty) ++
              x.audioFormat.map{y => implicitly[Writer[AudioFormatType,NodeSeq]].write(y)}.getOrElse(Empty) ++
              x.dataFormat.map{y => implicitly[Writer[DataFormatType,NodeSeq]].write(y)}.getOrElse(Empty) ++
              x.containerFormat.map{y => implicitly[Writer[ContainerFormatType,NodeSeq]].write(y)}.getOrElse(Empty)
            }</bms:formatCollection>} ++
          contentFormat.duration.map{x => <bms:duration>{implicitly[Writer[Option[Duration],NodeSeq]].write(Some(x))}</bms:duration>}.getOrElse(Empty) ++
          contentFormat.hash.map{x => <bms:hash><bms:hashFunction>{Text(x.hashFunction.toString)}</bms:hashFunction><bms:value>{
            Text(HexBinary.toHex(x.value))}</bms:value></bms:hash>}.getOrElse(Empty) ++
          contentFormat.packageSize.map{x => <bms:packageSize>{Text(x.toString)}</bms:packageSize>}.getOrElse(Empty) ++
          contentFormat.mimeType.map{x => writeTypeGroup(<bms:mimeType>{Text(x.value)}</bms:mimeType>, x)}.getOrElse(Empty)}</bms:bmContentFormat>,
          Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      BMContentFormat(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          bmEssenceLocators = nodes \ "bmEssenceLocators" match { case Empty => Nil; case xml => xml \ "bmEssenceLocator" map {
            x => implicitly[Reader[BMEssenceLocatorType,NodeSeq]].read(x)}},
          formatCollection = nodes \ "formatCollection" match { case Empty => None; case collection => Some(FormatCollection(
              videoFormat = collection \ "videoFormat" match { case Empty => None; case xml => Some(implicitly[Reader[VideoFormatType,NodeSeq]].read(xml)) },
              audioFormat = collection \ "audioFormat" match { case Empty => None; case xml => Some(implicitly[Reader[AudioFormatType,NodeSeq]].read(xml)) },
              dataFormat = collection \ "dataFormat" match { case Empty => None; case xml => Some(implicitly[Reader[DataFormatType,NodeSeq]].read(xml))} ,
              containerFormat = collection \ "containerFormat" match { case Empty => None; case xml => Some(implicitly[Reader[ContainerFormatType,NodeSeq]].read(xml))})) },
          duration = nodes \ "duration" match { case Empty => None; case xml => implicitly[Reader[Option[Duration],NodeSeq]].read(xml.head.child)},
          hash = nodes \ "hash" match { case Empty => None; case xml => Some(Hash(
              HashFunctionTypes.fromString(xml \ "hashFunction" text),
              HexBinary.fromHex(xml \ "value" text)))},
          packageSize = nodes \ "packageSize" match { case Empty => None; case xml => Some(xml.text.toLong)},
          mimeType = nodes \ "mimeType" match { case Empty => None; case xml => Some(readTypeGroup(xml, MimeType(value = xml text)))}
      )
    }
  }
  
  implicit object DescriptionConverter extends Converter[DescriptionType,NodeSeq] {
    def write(description: DescriptionType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:description>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(description.resourceParameters) ++
          (description.bmContentDescription match {
            case None => Empty
            case Some(details) => <desc:bmContentDescription>{
              details.title.map{x => <desc:title>{Text(x.value)}</desc:title> % 
                x.lang.map{y => scala.xml.Attribute("xml", "lang", Text(y), scala.xml.Null)}.getOrElse(scala.xml.Null)} ++
              details.alternativeTitle.map{x => writeDescriptionTypeGroup(<desc:alternativeTitle>{Text(x.value)}</desc:alternativeTitle> %
                x.lang.map{y => scala.xml.Attribute("xml", "lang", Text(y), scala.xml.Null)}.getOrElse(scala.xml.Null), x)} ++
              details.description.map{x => writeDescriptionTypeGroup(<desc:description>{Text(x.value)}</desc:description> %
                x.lang.map{y => scala.xml.Attribute("xml", "lang", Text(y), scala.xml.Null)}.getOrElse(scala.xml.Null), x)} ++
              details.identifier.map{x => writeDescriptionTypeGroup(writeDescriptionFormatGroup(
                  <desc:identifier>{Text(x.value)}</desc:identifier>, x), x)} ++
              details.version.map{x => <desc:version>{Text(x)}</desc:version>}.getOrElse(Empty)    
              }</desc:bmContentDescription> % details.lang.map{x => scala.xml.Attribute("xml", "lang", Text(x), scala.xml.Null)}.getOrElse(scala.xml.Null)
            })
          }</bms:description>,
          Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      val langPattern = "@{" + Namespaces.xml.uri + "}lang"
      Description(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          bmContentDescription = nodes \ "bmContentDescription" match {
            case Empty => None
            case xml => Some(BMContentDescription(
                title = xml \ "title" map {x => TextElement(x.text, x \ langPattern match { case Empty => None; case lang => Some(lang.text.trim)})},
                alternativeTitle = xml \ "alternativeTitle" map {x => readDescriptionTypeGroup(x, 
                    Title(value = x.text, lang = x \ langPattern match { case Empty => None; case lang => Some(lang.text.trim)}))},
                description = xml \ "description" map {x => readDescriptionTypeGroup(x,
                    ContentDescription(value = x.text, lang = x \ langPattern match { case Empty => None; case lang => Some(lang.text.trim)}))},
                identifier = xml \ "identifier" map {x => readDescriptionTypeGroup(x, readDescriptionFormatGroup(x,
                    Identifier(value = x.text)))},
                version = xml \ "version" match { case Empty => None; case v => Some(v.text) },
                lang = xml \ langPattern match { case Empty => None; case l => Some(l.text.trim)}
            ))
          }
      )
    }
  }
  
  implicit object BMContentConverter extends Converter[BMContentType,NodeSeq] {
    def write(content: BMContentType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:bmContent>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(content.resourceParameters) ++
          (content.bmContentFormats match { 
            case Seq() => Empty 
            case formats @ Seq(_*) => <bms:bmContentFormats>{
              formats.map{x => implicitly[Writer[BMContentFormatType,NodeSeq]].write(x)}}</bms:bmContentFormats>
          }) ++
          (content.descriptions match {
            case Seq() => Empty
            case descriptions @ Seq(_*) => <bms:descriptions>{
              descriptions.map{x => implicitly[Writer[DescriptionType,NodeSeq]].write(x)}}</bms:descriptions>
          })}</bms:bmContent>,
          Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      BMContent( 
        implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
        nodes \ "bmContentFormats" match { case Empty => Nil; case xml => xml \ "bmContentFormat" map {x => implicitly[Reader[BMContentFormatType,NodeSeq]].read(x)}},
        nodes \ "descriptions" match { case Empty => Nil; case xml => xml \ "description" map {x => implicitly[Reader[DescriptionType,NodeSeq]].read(x)} })
    }
  }
  
  implicit object BMObjectConverter extends Converter[BMObjectType,NodeSeq] {
    def write(thing: BMObjectType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:bmObject>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(thing.resourceParameters) ++
          (thing.bmContents match {
            case Seq() => Empty
            case contents @ Seq(_*) => <bms:bmContents>{
              contents.map{x => implicitly[Writer[BMContentType,NodeSeq]].write(x)}}</bms:bmContents>
          })}</bms:bmObject>,
          Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      BMObject(
          implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          nodes \ "bmContents" match { case Empty => Nil; case xml => xml \ "bmContent" map{x => implicitly[Reader[BMContentType,NodeSeq]].read(x)}}
      )
    }
  }
  
  implicit object QueueConverter extends Converter[QueueType, NodeSeq] {
    def write(queue: QueueType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:queue>{
          implicitly[Writer[ResourceParameters,NodeSeq]].write(queue.resourceParameters) ++
          queue.status.map{x => <bms:status>{Text(x.toString)}</bms:status>}.getOrElse(Empty) ++
          queue.statusDescription.map{x => <bms:statusDescription>{Text(x)}</bms:statusDescription>}.getOrElse(Empty) ++
          queue.length.map{x => <bms:length>{Text(x.toString)}</bms:length>}.getOrElse(Empty) ++
          queue.availability.map{x => <bms:availability>{Text(x.toString)}</bms:availability>}.getOrElse(Empty) ++
          queue.estimatedTotalCompletionDuration.map{x => <bms:estimatedTotalCompletionDuration>{Text(XMLDurationConverter.toString(x))}</bms:estimatedTotalCompletionDuration>}.getOrElse(Empty) ++
          (queue.jobs match {
            case Seq() => Empty
            case jobs @ Seq(_*) => <bms:jobs>{ // TODO support complete job serialization
              jobs.map{x => <bms:job xsi:type="cms:CaptureJobType">{implicitly[Writer[ResourceParameters,NodeSeq]].write(x.resourceParameters)}</bms:job>}}</bms:jobs>
            })
          }</bms:queue>, 
          Namespaces.cms)
    }
    def read(nodes: NodeSeq) = {
      Queue(
          resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          status = nodes \ "status" match { case Empty => None; case xml => Some(QueueStatusType.fromString(xml.text.trim.toLowerCase))},
          statusDescription = nodes \ "statusDescription" match { case Empty => None; case xml => Some(xml.text.trim)},
          length = nodes \ "length" match { case Empty => None; case xml => Some(xml.text.trim.toInt)},
          availability = nodes \ "availability" match { case Empty => None; case xml => Some(xml.text.trim.toBoolean)},
          estimatedTotalCompletionDuration = nodes \ "estimatedTotalCompletionDuration" match { case Empty => None; case xml => Some(XMLDurationConverter.fromString(xml.text.trim))},
          jobs = nodes \ "jobs" match {
            case Empty => Nil
            case xml => 
              xml \ "job" map {x => CaptureJob(resourceParameters = implicitly[Reader[ResourceParameters,NodeSeq]].read(x), BaseJobParameters(), CaptureJobParameters())}
          }
      )
    }
  }
  
  implicit object BaseJobParametersConverter extends Converter[BaseJobParameters, NodeSeq] {
    def write(parameters: BaseJobParameters) = {
      parameters.status.map(x => <bms:status>{Text(x.toString)}</bms:status>).getOrElse(Empty) ++
      parameters.statusDescription.map(x => <bms:statusDescription>{Text(x)}</bms:statusDescription>).getOrElse(Empty) ++
      parameters.serviceProviderJobID.map(x => <bms:serviceProviderJobID>{Text(x)}</bms:serviceProviderJobID>).getOrElse(Empty) ++
      parameters.queueReference.map{x => <bms:queueReference>{implicitly[Writer[ResourceParameters,NodeSeq]].write(x.resourceParameters)}</bms:queueReference>}.getOrElse(Empty) ++
      (parameters.tasks match {
        case Seq() => Empty
        case tasks @ Seq(_*) => <bms:tasks>{tasks.map{x => <bms:job>{implicitly[Writer[ResourceParameters,NodeSeq]].write(x.resourceParameters)}</bms:job>}}</bms:tasks>
      }) ++
      parameters.operationName.map(x => <bms:operationName>{Text(x)}</bms:operationName>).getOrElse(Empty) ++
      (parameters.bmObjects match { 
        case Seq() => Empty
        case objects @ Seq(_*) => <bms:bmObjects>{objects.map{x => implicitly[Writer[BMObjectType,NodeSeq]].write(x)}}</bms:bmObjects>
      }) ++
      parameters.priority.map(x => <bms:priority>{Text(x.toString)}</bms:priority>).getOrElse(Empty) ++
      parameters.startJob.map(x => <bms:startJob xsi:type={x match {
          case noWait : StartJobByNoWait => "bms:StartJobByNoWaitType"
          case time : StartJobByTime => "bms:StartJobByTimeType"
          case latest : StartJobByLatest => "bms:StartJobByLatestType"
        }}>{implicitly[Writer[StartJobType,NodeSeq]].write(x)}</bms:startJob>).getOrElse(Empty) ++
      parameters.finishBefore.map(x => <bms:finishBefore>{Text(XMLDateTimeConverter.toString(x))}</bms:finishBefore>).getOrElse(Empty) ++
      parameters.estimatedCompletionDuration.map(x => <bms:estimatedCompletionDuration>{Text(XMLDurationConverter.toString(x))}</bms:estimatedCompletionDuration>).getOrElse(Empty) ++
      parameters.currentQueuePosition.map(x => <bms:currentQueuePosition>{Text(x.toString)}</bms:currentQueuePosition>).getOrElse(Empty) ++
      parameters.jobStartedTime.map(x => <bms:jobStartedTime>{Text(XMLDateTimeConverter.toString(x))}</bms:jobStartedTime>).getOrElse(Empty) ++
      parameters.jobElapsedTime.map(x => <bms:jobElapsedTime>{Text(XMLDurationConverter.toString(x))}</bms:jobElapsedTime>).getOrElse(Empty) ++
      parameters.jobCompletedTime.map(x => <bms:jobCompletedTime>{Text(XMLDateTimeConverter.toString(x))}</bms:jobCompletedTime>).getOrElse(Empty) ++
      parameters.processed.map(x => <bms:processed xsi:type={x match { 
        case bytes : ProcessedInfoByBytes => "bms:ProcessedInfoByBytesType"
        case xframes : ProcessedInfoByFrames => "bms:ProcesseInfoByFramesType" 
       }}>{implicitly[Writer[Option[ProcessedInfoType],NodeSeq]].write(Some(x))}</bms:processed>).getOrElse(Empty)
    }
    def read(nodes: NodeSeq) : BaseJobParameters = {
      BaseJobParameters(
          status = nodes \ "status" match { case Empty => None; case xml => Some(JobStatusType.fromString(xml.text.trim)) },
          statusDescription = nodes \ "statusDescription" match { case Empty => None; case xml => Some(xml.text.trim) },
          serviceProviderJobID = nodes \ "serviceProviderJobID" match { case Empty => None; case xml => Some(xml.text.trim) },
          queueReference = nodes \ "queueReference" match { case Empty => None; case xml =>
            Some(Queue(implicitly[Reader[ResourceParameters,NodeSeq]].read(xml)))},
          tasks = nodes \ "tasks" match { // TODO support full job serialization
              case Empty => Nil
              case xml => xml \ "job" map {x => CaptureJob(implicitly[Reader[ResourceParameters,NodeSeq]].read(x), BaseJobParameters(), CaptureJobParameters())}
            },
          operationName = nodes \ "operationName" match { case Empty => None; case xml => Some(xml.text.trim) },
          bmObjects = nodes \ "bmObjects" match {
            case Empty => Nil
            case xml => xml \ "bmObject" map {x => implicitly[Reader[BMObjectType,NodeSeq]].read(x)}
          },
          priority = nodes \ "priority" match { case Empty => None; case xml => Some(PriorityType.fromString(xml.text.trim)) },
          startJob = nodes \ "startJob" match { case Empty => None; case xml => Some(implicitly[Reader[StartJobType, NodeSeq]].read(xml)) },
          finishBefore = nodes \ "finishBefore" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim)) },
          estimatedCompletionDuration = nodes \ "estimatedCompletionDuration" match { case Empty => None; case xml => Some(XMLDurationConverter.fromString(xml.text.trim)) },
          currentQueuePosition = nodes \ "currentQueuePosition" match { case Empty => None; case xml => Some(xml.text.trim.toInt)},
          jobStartedTime = nodes \ "jobStartedTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim)) },
          jobElapsedTime = nodes \ "jobElapsedTime" match { case Empty => None; case xml => Some(XMLDurationConverter.fromString(xml.text.trim)) },
          jobCompletedTime = nodes \ "jobCompletedTime" match { case Empty => None; case xml => Some(XMLDateTimeConverter.fromString(xml.text.trim)) },
          processed = implicitly[Reader[Option[ProcessedInfoType],NodeSeq]].read(nodes \ "processed")
      )
    }
  }
  
  implicit object CaptureJobConverter extends Converter[CaptureJobType, NodeSeq] {
    def write(job: CaptureJobType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:job xsi:type="cms:CaptureJobType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(job.resourceParameters) ++
        implicitly[Writer[BaseJobParameters,NodeSeq]].write(job.baseParameters) ++
        implicitly[Writer[CaptureJobParameters,NodeSeq]].write(job.serviceParameters)     
      }</bms:job>, Namespaces.cms)
    }    
    def read(nodes: NodeSeq): CaptureJobType = {
      CaptureJob(
          implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseJobParameters,NodeSeq]].read(nodes),
          implicitly[Reader[CaptureJobParameters,NodeSeq]].read(nodes)
      )
    }
  }

  implicit object TransferJobConverter extends Converter[TransferJobType, NodeSeq] {
    def write(job: TransferJobType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:job xsi:type="tms:TransferJobType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(job.resourceParameters) ++
        implicitly[Writer[BaseJobParameters,NodeSeq]].write(job.baseParameters) ++
        implicitly[Writer[TransferJobParameters,NodeSeq]].write(job.serviceParameters)
      }</bms:job>, Namespaces.tms)
    }
    def read(nodes: NodeSeq): TransferJobType = {
      TransferJob(
          implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseJobParameters,NodeSeq]].read(nodes),
          implicitly[Reader[TransferJobParameters,NodeSeq]].read(nodes)
      )
    }
  }
  
  implicit object TransformJobConverter extends Converter[TransformJobType, NodeSeq] {
    def write(job: TransformJobType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:job xsi:type="tfms:TransformJobType">{
        implicitly[Writer[ResourceParameters,NodeSeq]].write(job.resourceParameters) ++
        implicitly[Writer[BaseJobParameters,NodeSeq]].write(job.baseParameters) ++
        implicitly[Writer[TransformJobParameters,NodeSeq]].write(job.serviceParameters)
      }</bms:job>, Namespaces.tfms)      
    }
    def read(nodes: NodeSeq): TransformJobType = {
      TransformJob(
          implicitly[Reader[ResourceParameters,NodeSeq]].read(nodes),
          implicitly[Reader[BaseJobParameters,NodeSeq]].read(nodes),
          implicitly[Reader[TransformJobParameters,NodeSeq]].read(nodes))
    }
  }
  
  implicit object InnerFaultConverter extends Converter[InnerFault,NodeSeq] {
    def write(fault: InnerFault) = {
      <bms:code>{Text(fault.code)}</bms:code> ++
      fault.description.map{x => <bms:description>{Text(x)}</bms:description>}.getOrElse(Empty) ++
      fault.detail.map{x => <bms:detail>{Text(x)}</bms:detail>}
    }
    def read(nodes: NodeSeq) = {
      InnerFault(
          code = (nodes \ "code").text.trim,
          description = nodes \ "description" match { case Empty => None; case xml => Some(xml.text.trim) },
          detail = nodes \ "detail" match { case Empty => None; case xml => Some(xml.text.trim) }
      )
    }
  }
   
  implicit object FaultConverter extends Converter[FaultType,NodeSeq] {
    def write(fault: FaultType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:fault>{
        <bms:code>{Text(fault.code.toString)}</bms:code> ++
        fault.description.map{x => <bms:description>{Text(x)}</bms:description>}.getOrElse(Empty) ++
        fault.detail.map{x => <bms:detail>{x match { case null => Text(""); case _ => Text(x)}}</bms:detail>} ++
        fault.innerFault.map{x => <bms:innerFault>{implicitly[Writer[InnerFault,NodeSeq]].write(x)}</bms:innerFault>}
        }</bms:fault>, 
        Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      Fault(
          code = ErrorCodeType.fromString((nodes \ "code").text.trim.toUpperCase),
          description = nodes \ "description" match { case Empty => None; case xml => Some(xml.text.trim) },
          detail = nodes \ "detail" match { case Empty => None; case xml => Some(xml.text.trim) },
          innerFault = nodes \ "innerFault" map {x => implicitly[Reader[InnerFault,NodeSeq]].read(x)}
      )
    }
  }
  
  implicit object CaptureFaultConverter extends Converter[CaptureFaultType,NodeSeq] {
    def write(fault: CaptureFaultType) = {
      implicitly[Writer[FaultType,NodeSeq]].write(fault).head match {
        case e: Elem => e.copy(scope = Namespaces.cms, child = e.child ++ 
            fault.extendedCode.map{x => <extendedCode>{Text(x.toString)}</extendedCode>}.getOrElse(Empty)) % 
            scala.xml.Attribute("xsi", "type", Text("cms:CaptureFaultType"), scala.xml.Null)
        case _ => Empty    
      }
    }  
    def read(nodes: NodeSeq) = {
      val baseFault = FaultConverter.read(nodes)
      CaptureFault(
          code = baseFault.code, description = baseFault.description, detail = baseFault.detail, innerFault = baseFault.innerFault,
          extendedCode = nodes \ "extendedCode" match { case Empty => None; case xml => Some(CaptureErrorCodeType.fromString(xml.text.trim.toUpperCase)) }
      )
    }
  }
  
  implicit object ManageJobRequestConverter extends Converter[ManageJobRequestType, NodeSeq] {
    import StringConverters.ResourceIDConverter
    def write(manageJob: ManageJobRequestType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:manageJobRequest>{
        <bms:jobID>{Text(FimsString.write(manageJob.jobID))}</bms:jobID> ++
        <bms:jobCommand>{Text(manageJob.jobCommand.toString)}</bms:jobCommand> ++
        manageJob.priority.map {x => <bms:priority>{Text(x.toString)}</bms:priority>}.getOrElse(Empty) ++
        manageJob.extensionGroup.map{x => <bms:ExtensionGroup>{x}</bms:ExtensionGroup>}.getOrElse(Empty) ++
        manageJob.extensionAttributes.map{x => <bms:ExtensionAttributes/> % x}.getOrElse(Empty)
      }</bms:manageJobRequest>, Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      ManageJobRequest(
          jobID = FimsString.read(nodes \ "jobID" text),
          jobCommand = nodes \ "jobCommand" match { case xml => JobCommandType.fromString(xml.text.trim) },
          priority = nodes \ "priority" match { case Empty => None; case xml => Some(PriorityType.fromString(xml.text.trim)) },
          extensionGroup = nodes \ "ExtensionGroup" match { case Empty => None; case xml => Some(xml.head.child) },
          extensionAttributes = nodes \ "extensionAttributes" match { case Empty => None; case xml => Some(xml.head.attributes) })
    }
  }
  
  implicit object ManageQueueRequestConverter extends Converter[ManageQueueRequestType, NodeSeq] {
    import StringConverters.ResourceIDConverter
    def write(manageQueue: ManageQueueRequestType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:manageQueueRequest>{
        <bms:queueID>{Text(FimsString.write(manageQueue.queueID))}</bms:queueID> ++
        <bms:queueCommand>{Text(manageQueue.queueCommand.toString)}</bms:queueCommand> ++
        manageQueue.extensionGroup.map{x => <bms:ExtensionGroup>{x}</bms:ExtensionGroup>}.getOrElse(Empty) ++
        manageQueue.extensionAttributes.map{x => <bms:ExtensionAttributes/> % x}.getOrElse(Empty)
      }</bms:manageQueueRequest>, Namespaces.bms)
    }
    def read(nodes: NodeSeq) = {
      ManageQueueRequest(
          queueID = FimsString.read(nodes \ "queueID" text),
          queueCommand = nodes \ "queueCommand" match { case xml => QueueCommandType.fromString(xml.text.trim) },
          extensionGroup = nodes \ "ExtensionGroup" match { case Empty => None; case xml => Some(xml.head.child) },
          extensionAttributes = nodes \ "extensionAttributes" match { case Empty => None; case xml => Some(xml.head.attributes) })
    }
  }
  
  // Not set to implicit because this breaks other resource resolution.
  object ResourceReferenceConverter extends Converter[ResourceReferenceType, NodeSeq] {
    import StringConverters.ResourceIDConverter
    def write(resourceReference: ResourceReferenceType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<bms:resourceReference>{
        writeBodyNodes(resourceReference)}
      </bms:resourceReference>, Namespaces.bms)
    }
    def writeBodyNodes(resourceReference: ResourceReferenceType) = {
      <bms:resourceID>{Text(FimsString.write(resourceReference.resourceID))}</bms:resourceID> ++
      resourceReference.revisionID.map{x => <bms:revisionID>{Text(x)}</bms:revisionID>}.getOrElse(Empty) ++
      resourceReference.location.map{x => <bms:location>{Text(x.toString())}</bms:location>}.getOrElse(Empty)
    }
    def read(nodes: NodeSeq) = {
      ResourceReference(
          resourceID = FimsString.read(nodes \ "resourceID" text),
          revisionID = nodes \ "revisionID" match { case Empty => None; case xml => Some(xml.text.trim) },
          location = nodes \ "location" match { case Empty => None; case xml => Some(new java.net.URI(xml.text.trim)) })
    }
  } 
  
  implicit object RespositoryFaultConverter extends Converter[RepositoryFaultType,NodeSeq] {
    def write(fault: RepositoryFaultType) = {
      implicitly[Writer[FaultType,NodeSeq]].write(fault).head match {
        case e: Elem => e.copy(scope = Namespaces.cms, child = e.child ++ 
            fault.extendedCode.map{x => <extendedCode>{Text(x.toString)}</extendedCode>}.getOrElse(Empty)) % 
            scala.xml.Attribute("xsi", "type", Text("rps:RepositoryFaultType"), scala.xml.Null)
        case _ => Empty    
      }
    }  
    def read(nodes: NodeSeq) = {
      val baseFault = FaultConverter.read(nodes)
      RepositoryFault(
          code = baseFault.code, description = baseFault.description, detail = baseFault.detail, innerFault = baseFault.innerFault,
          extendedCode = nodes \ "extendedCode" match { case Empty => None; case xml => Some(RepositoryErrorCodeType.fromString(xml.text.trim.toUpperCase)) }
      )
    }
  }

  implicit object PropertyInfoConverter extends Converter[PropertyInfoType, NodeSeq] {
    def write(propertyInfo: PropertyInfoType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<rps:propertyInfo>{
        <rps:targetResourceReference>{
          ResourceReferenceConverter.writeBodyNodes(propertyInfo.targetResourceReference)}</rps:targetResourceReference> ++
        <rps:address>{Text(propertyInfo.address)}</rps:address> ++
        <rps:action>{Text(propertyInfo.action.toString)}</rps:action> ++
        propertyInfo.value.map {x => <rps:value>{Text(x)}</rps:value>}.getOrElse(Empty)}</rps:propertyInfo>, Namespaces.rps)
    }
    def read(nodes: NodeSeq) = {
      PropertyInfo(
          targetResourceReference = ResourceReferenceConverter.read(nodes \ "targetResourceReference"), 
          address = (nodes \ "address").text.trim, 
          action = RepositoryActionType.fromString((nodes \ "action").text.trim),
          value = (nodes \ "value") match { case Empty => None; case xml => Some(xml.text.trim) })
    }
  } 
  
  implicit object PropertyInfosReader extends Reader[Seq[PropertyInfoType], NodeSeq] {
    def read(nodes: NodeSeq) = {
      nodes \ "propertyInfos" map { xml => implicitly[Reader[PropertyInfoType, NodeSeq]].read(xml)}
    }
  }
  
  implicit object PurgeContentOperationAckConverter extends Converter[PurgeContentOperationAckType, NodeSeq] {
    def write(pcoa: PurgeContentOperationAckType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<rps:purgeContentOperationAck>{
        <rps:timeStamp>{Text(XMLDateTimeConverter.toString(pcoa.timeStamp))}</rps:timeStamp> ++
        <rps:operationID>{ResourceReferenceConverter.writeBodyNodes(pcoa.operationID)}</rps:operationID>
      }</rps:purgeContentOperationAck>, Namespaces.rps)
    }
    def read(nodes: NodeSeq) = {
      PurgeContentOperationAck(
          timeStamp = XMLDateTimeConverter.fromString((nodes \ "timeStamp").text.trim),
          operationID = ResourceReferenceConverter.read(nodes \ "operationID"))
    }
  }
  
  implicit object PurgeEssenceOperationAckConverter extends Converter[PurgeEssenceOperationAckType, NodeSeq] {
    def write(peoa: PurgeEssenceOperationAckType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<rps:purgeEssenceOperationAck>{
        <rps:timeStamp>{Text(XMLDateTimeConverter.toString(peoa.timeStamp))}</rps:timeStamp> ++
        <rps:operationID>{ResourceReferenceConverter.writeBodyNodes(peoa.operationID)}</rps:operationID>        
      }</rps:purgeEssenceOperationAck>, Namespaces.rps)
    }
    def read(nodes: NodeSeq) = {
      PurgeEssenceOperationAck(
          timeStamp = XMLDateTimeConverter.fromString((nodes \ "timeStamp").text.trim),
          operationID = ResourceReferenceConverter.read(nodes \ "operationID"))
    }
  }
  
  implicit object AddEssenceRequestConverter extends Converter[AddEssenceRequestType, NodeSeq] {
    def write(aer: AddEssenceRequestType) = ???
    def read(nodes: NodeSeq) = ???
  }
  
  implicit object AddEssenceOperationAckConverter extends Converter[AddEssenceOperationAckType, NodeSeq] {
    def write(aeoa: AddEssenceOperationAckType) = {
      XMLNamespaceProcessor.setNameSpaceIfAbsent(<rps:addEssenceOperationAck>{
        <rps:timeStamp>{Text(XMLDateTimeConverter.toString(aeoa.timeStamp))}</rps:timeStamp> ++
        <rps:operationID>{ResourceReferenceConverter.writeBodyNodes(aeoa.operationID)}</rps:operationID>        
      }</rps:addEssenceOperationAck>, Namespaces.rps)      
    }
    def read(nodes: NodeSeq) = {
      AddEssenceOperationAck(
          timeStamp = XMLDateTimeConverter.fromString((nodes \ "timeStamp").text.trim),
          operationID = ResourceReferenceConverter.read(nodes \ "operationID"))
    }
  } 
}

object XMLNamespaceProcessor {
  import scala.collection.mutable.HashSet
  def join(parent: NamespaceBinding, child: NamespaceBinding): NamespaceBinding = child.parent match {
    case null => parent
    case TopScope if (contains(parent, child.prefix, child.uri)) => parent 
    case TopScope => new NamespaceBinding(child.prefix, child.uri, parent)
    case parent if (contains(parent, child.prefix, child.uri)) => join(child.parent, parent)
    case parent => new NamespaceBinding(child.prefix, child.uri, join(child.parent, parent))
  }
//  def distinct(bindings: NamespaceBinding, seen: HashSet[Pair[String, String]] = HashSet[Pair[String, String]]()): NamespaceBinding = {
//    bindings match {
//      case TopScope => TopScope
//      case _ =>
//        if (!seen((bindings.prefix, bindings.uri))) {
//          seen += ((bindings.prefix, bindings.uri))
//          bindings.parent match {
//            case TopScope => bindings
//            case nsb => new NamespaceBinding(bindings.prefix, bindings.uri, distinct(nsb, seen))
//          }
//        }
//        else distinct(bindings.parent, seen)
//    }
//  }
  def contains(binding: NamespaceBinding, prefix: String, uri: String): Boolean = binding.parent match {
    case TopScope => binding.prefix == prefix && binding.uri == uri
    case parent => 
      if (binding.prefix == prefix && binding.uri == uri) true
      else contains(parent, prefix, uri)
  }
  def subTree(a: NamespaceBinding, b: NamespaceBinding): Boolean = b match {
    case null => false
    case TopScope => a == TopScope
    case t if t == a => true
    case t => subTree(a, t.parent)
  }
  def setNameSpaceIfAbsent(input: Seq[Node], _scope: NamespaceBinding): Seq[Node] = {
    for (node <- input) yield node match {
      case elem: Elem =>
        val children = elem.child.toSeq
        val newScope = if (elem.scope == TopScope) _scope 
          else if (subTree(elem.scope, _scope)) _scope 
          else if (subTree(_scope, elem.scope)) elem.scope
          else new NamespaceBinding(elem.scope.prefix, elem.scope.uri, _scope) // TODO support more than one extension namespace per element
        elem.copy(scope = newScope, child = setNameSpaceIfAbsent(children, newScope))
      case other => other
    }
  }
}

object FimsXML{
  
  def toStream(xml : Node, stream : OutputStream, encoding : String) = {
      IOUtil.withWriter(new OutputStreamWriter(stream,encoding)){osw =>
     	XML.write(osw, xml, encoding, true, null)
     }
  }
  
  def toStream[A](value: A, stream : OutputStream, encoding : String )(implicit xmlWriter: Writer[A,NodeSeq]) : Unit = toStream(write(value).apply(0), stream, encoding)

  def fromStream[A](stream : InputStream, encoding : String)(implicit xmlReader: Reader[A,NodeSeq]) : A = {
      IOUtil.withReader(new InputStreamReader(stream,encoding)){isr =>
        val xml = XML.load(isr)
        read[A](xml)
      }
  }
  
  def read[A](nodes: NodeSeq)(implicit xmlReader: Reader[A,NodeSeq]): A = xmlReader.read(scala.xml.Utility.trim(nodes.head))
  
  def write[A](value: A)(implicit xmlWriter: Writer[A,NodeSeq]): Node = 
    scala.xml.Utility.trimProper(xmlWriter.write(value)(0))(0)
  
  def writeAll[A](value: A)(implicit xmlWriter: Writer[A,NodeSeq]): NodeSeq = xmlWriter.write(value)
}
