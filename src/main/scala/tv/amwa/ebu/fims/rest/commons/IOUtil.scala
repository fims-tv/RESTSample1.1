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

package tv.amwa.ebu.fims.rest.commons
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer
import java.io.Reader
import java.io.ByteArrayOutputStream

/**
 * Collection of methods using the loan pattern to safely use input and output streams|writers.
 */
object IOUtil{
  def withInputStream[S <: InputStream,T]( is : S)(fn : S => T) : T = {
    try{
      val t = fn(is)
      t
    }finally{
      is.close
    }
  }

  def withOutputStream[S <: OutputStream,T]( os : S)(fn : S => T) : T = {
    try{
      val t = fn(os)
      os.flush
      t
    }finally{
      os.close
    }
  }

  def withWriter[W <: Writer,T]( w : W)(fn : W => T) : T = {
    try{
      val t = fn(w)
      w.flush
      t
    }finally{
      w.close
    }
  }

  def withReader[R <: Reader,T]( r : R)(fn : R => T) : T = {
    try{
      fn(r)
    }finally{
      r.close
    }
  }

  def copy(src : InputStream, dest : OutputStream) : Unit = {
    val buffer = new Array[Byte](8192)
    Iterator.continually(src.read(buffer)).takeWhile(p => p != -1).foreach { i => dest.write(buffer, 0, i) }
    dest.flush
  }
  
  def readAllBytes(src : InputStream) : Array[Byte] = {
    IOUtil.withOutputStream(new ByteArrayOutputStream){baos=>
   	  IOUtil.copy(src,baos)
   	  baos.toByteArray()
  	}
  }
}