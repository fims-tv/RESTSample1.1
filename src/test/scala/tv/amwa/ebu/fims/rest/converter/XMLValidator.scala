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
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.{Validator=>JValidator}
import org.xml.sax.SAXException
import java.io.InputStream
import java.io.StringReader

object XMLValidator {
  def parentURI(child: String): String = child.split("/").dropRight(1).mkString("", "/", "/")
  def validate(xmlContent: String, xsdFile: String): Boolean = {
    try {
      val schemaLang = "http://www.w3.org/2001/XMLSchema"
      val factory = SchemaFactory.newInstance(schemaLang)
      val schema = factory.newSchema(new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile),
          parentURI(Thread.currentThread().getContextClassLoader().getResource(xsdFile).toString)))
      val validator = schema.newValidator()
      validator.validate(new StreamSource(new StringReader(xmlContent)))
    } catch {
      case ex: SAXException => println(ex.getMessage()); return false
      case ex: Exception => ex.printStackTrace()
    }
    true
  }
}