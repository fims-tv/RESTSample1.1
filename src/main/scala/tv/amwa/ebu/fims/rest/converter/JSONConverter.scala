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

import scala.xml._
import java.io.OutputStream
import tv.amwa.ebu.fims.rest.commons.IOUtil
import java.io.OutputStreamWriter
import tv.amwa.ebu.fims.rest.converter.json.JSON
import scala.xml.NodeSeq.Empty
import java.io.InputStream
import java.io.InputStreamReader
import org.apache.commons.io.IOUtils
import scala.io.Codec
import scala.reflect.ClassTag

object JSONConverters {

  private def prefixedName(e : Elem): String =
    if (e.prefix == null) e.label else e.prefix + ":" + e.label 
  private def prefixedName(g : Group): String = g.nodes.head match { case e: Elem => prefixedName(e); case _ => "Unknwon" } 
  private def hasChildren(node : Node) =
    node.child.exists{_ match { case e: Elem => true; case _ => false }} || node.attributes.size > 0
  private def textOnlyChildren(node: Node) =
    node match { case g: Group => false; case _ => node.child.exists{_ match { case e: Text => true; case _ => false}} && node.attributes.size > 0 }
  private def repeatedChild(node: Node, prefix: String, label: String) =
    node.child.map{x => x match { case e: Elem => Some((e.prefix, e.label)); case _ => None}}.filter(_ == Some((prefix, label))).size > 1
  private def joinPairs(nodes: NodeSeq): NodeSeq =
    nodes.foldRight(Empty){(x, y) => x match {
      case e : Elem => y.headOption.getOrElse(Empty) match {
        case f: Elem if (e.prefix, e.label) == (f.prefix, f.label) => List(Group(x ++ y.head)).toSeq ++ y.tail
        case g: Group if (e.prefix, e.label) == (g.head.prefix, g.head.label) => List(Group(x ++ g.nodes)).toSeq ++ y.tail
        case _ => x ++ y
      }
      case _ => x ++ y
    }}
  private def realToJSON(node : Node, indent : String, textOnly: Boolean = false, inGroup: Boolean = false) : String = node match {
    case g : Group => "\"" + prefixedName(g) + "\" : [" + g.nodes.map{x => 
      realToJSON(x, indent + "  ", textOnlyChildren(x), true)}.mkString(",\n" + indent) + "]"
    case e : Elem => (if (!inGroup) ("\"" + prefixedName(e) + "\" : ") else "") +
    		(if (hasChildren(e)) "{\n" + indent else "") +
    		(if ((e.child.size > 0) || (e.attributes.size > 0)) 
    		  attributes(e, indent) + (if ((e.attributes.size > 0) && (e.child.size > 0)) ",\n" + indent else "") +
    		    (if (textOnly) List("\"#text\" : " + (e.child.text match { case z if z matches """\d+(\.\d+)?|true|false""" => z; case z => "\"" + z + "\""})).toSeq 
    		        else joinPairs(e.child).map{x => realToJSON(x, indent + "  ", textOnlyChildren(x))}).mkString(",\n" + indent) else "{ }") + 
     		(if (hasChildren(e)) "}" else "")	
    case t : Text if t.data matches """\d+(\.\d+)?|true|false""" => t.data
    case t : Text => "\"" + t.text + "\""
    case cd : PCData => "\"#cdata\" : \"" + cd.text + "\""
    case other => "*** Unknown *** - " + other.getClass.getCanonicalName
   }
  private def namespaces(nb : NamespaceBinding) : String = 
    "\"@xmlns:" + nb.prefix + "\" : \"" + nb.uri + "\",\n  " +
    		(if (nb.parent.prefix != null) namespaces(nb.parent) else "")
  private def attributes(e : Elem, indent : String) : String = 
    e.attributes.map{a => "\"@" + a.prefixedKey + "\" : \"" + a.value + "\""}.mkString(",\n" + indent) 
  def toJSON(elem: Elem) : String = "{\n  " + namespaces(elem.scope) + realToJSON(Utility.trim(elem), "    ") + "\n}"
  
  private def fromString(jsonString: String): Map[String, Any] = JSON.parseFull(jsonString).get match {
    case m : Map[String, _] => m
    case _ => throw new IllegalArgumentException("String is not a JSON object")
  }
  private def extractPrefix(name: String) = 
    if (name.contains(":")) (name.takeWhile(_ != ':'), name.dropWhile(_ != ':').tail) else (null, name)
  private def toNodes(json: Map[String, Any]): NodeSeq = json.flatMap{x => x match {
    case (name: String, value: String) if name.startsWith("@") => Empty
    case (name: String, value: Map[String, Any]) => { val details = extractPrefix(name) 
      Elem(details._1, details._2, toAttributes(value), toNamespaces(json), false, toNodes(value): _*) }
    case (name: String, value: String) if name == "#text" || name == "#cdata" => Text(value)
    case (name: String, value: String) => { val details = extractPrefix(name); Elem(details._1, details._2, scala.xml.Null, TopScope, false, Text(value)) }
    case (name: String, value: Boolean) => { val details = extractPrefix(name); Elem(details._1, details._2, scala.xml.Null, TopScope, false, Text(value.toString)) }
    case (name: String, value: Double) => { val details = extractPrefix(name); Elem(details._1, details._2, scala.xml.Null, TopScope, false,
        Text(if (value % 1.0 == 0.0) value.toLong.toString else value.toString)) }
    case (name: String, value: Seq[_]) => { val details = extractPrefix(name)
      value.flatMap{x => x match {
        case m : Map[String, Any] => Elem(details._1, details._2, toAttributes(m), toNamespaces(m), toNodes(m) : _*)
        case s : String => Elem(details._1, details._2, scala.xml.Null, TopScope, Text(s))
        case d : Double => Elem(details._1, details._2, scala.xml.Null, TopScope, Text(if (d % 1.0 == 0.0) d.toLong.toString else d.toString))
    }}}
    case _ => Empty
  }}.toSeq
  private def toAttributes(json: Map[String, Any]) = (for ((name, value) <- json; if name.startsWith("@") && !name.startsWith("@xmlns")) 
    yield (if (name.contains(':')) Attribute(name.takeWhile(_ != ':').tail, name.dropWhile(_ != ':').tail, Text(value.toString), Null)
      else Attribute(name.tail, Text(value.toString), Null))).fold(Null)(_ append _)
  private def toNamespaces(json: Map[String, Any]) = (for ((name, value) <- json; if name.startsWith("@xmlns"))
    yield(NamespaceBinding(name.dropWhile(_ != ':').tail, value.toString, TopScope))).fold(TopScope)((x, y) => NamespaceBinding(y.prefix, y.uri, x))

  def fromJSON(json: String): Elem = {
    val nodes = toNodes(fromString(json))
    XMLNamespaceProcessor.setNameSpaceIfAbsent(nodes, nodes.head.scope).head match {
      case root : Elem => root
      case _ => throw new IllegalArgumentException("Provided JSON could not be converted into FIMS XML.")
    }
  }
}

object FimsJSON {
  import tv.amwa.ebu.fims.rest.converter.FimsXML
  def toStream(json : String, stream : OutputStream, encoding : String) = 
      IOUtil.withWriter(new OutputStreamWriter(stream,encoding)){osw => osw.write(json)}
  def toStream[A](value: A, stream : OutputStream, encoding : String )(implicit xmlWriter: Writer[A,NodeSeq]) : Unit = toStream(write(value), stream, encoding)
  def fromStream[A](stream : InputStream, encoding : String)(implicit xmlReader: Reader[A,NodeSeq]) : A = {
    IOUtil.withInputStream(stream){is =>
      val json = scala.io.Source.fromInputStream(stream)(Codec(encoding)).mkString("")
      read[A](json)
    }
  }

  def write[A](value: A)(implicit xmlWriter: Writer[A,NodeSeq]): String = 
    JSONConverters.toJSON(xmlWriter.write(value)(0) match { case elem : Elem => elem; case _ => <UnrootedTree/>})
  def read[A](value: String)(implicit xmlReader: Reader[A,NodeSeq]): A = 
    xmlReader.read(JSONConverters.fromJSON(value))
}



