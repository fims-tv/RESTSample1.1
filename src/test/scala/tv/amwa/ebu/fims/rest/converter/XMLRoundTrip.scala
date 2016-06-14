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
import tv.amwa.ebu.fims.rest.commons.IOUtil
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import scala.xml.NodeSeq
import tv.amwa.ebu.fims.rest.Constants

object XMLRoundTrip {

  def writeToBytesThenRead[A](input: A)(implicit xmlReader: Reader[A,NodeSeq], xmlWriter: Writer[A,NodeSeq]): A = {
    IOUtil.withOutputStream(new ByteArrayOutputStream) { baos =>
      FimsXML.toStream[A](input, baos, Constants.DEFAULT_XML_ENCODING)
      IOUtil.withInputStream(new ByteArrayInputStream(baos.toByteArray)) { bais =>
        FimsXML.fromStream[A](bais, Constants.DEFAULT_XML_ENCODING)
      }
    }
  }

  def writewriteNodeThenRead[A](input: A)(implicit xmlReader: Reader[A,NodeSeq], xmlWriter: Writer[A, NodeSeq]): A = {
    val xml = FimsXML.write(input)
    FimsXML.read[A](xml)
  }
}