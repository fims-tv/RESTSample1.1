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

package tv.amwa.ebu.fims.rest.rest.handling
import javax.ws.rs.core.MultivaluedMap
import tv.amwa.ebu.fims.rest.converter.Reader
import tv.amwa.ebu.fims.rest.rest.model.ContainerType
import tv.amwa.ebu.fims.rest.rest.model.FullType
import tv.amwa.ebu.fims.rest.rest.model.LinkType
import tv.amwa.ebu.fims.rest.rest.model.SummaryType

object QueryParamsHandling {
  def getParam[T](name: String, queryParams : MultivaluedMap[String,String])(implicit reader : Reader[T,String]) : Either[Exception,Option[T]] = {
    Option(queryParams.getFirst(name)) match{
      case None => Right(None)
      case Some(v) => try{
        Right(Some(reader.read(v)))
      }catch{
        case ex : Exception => Left(ex)
      }
    }
  }
}

object Readers {
  implicit object IntReader extends Reader[Int,String] {
    def read(from:String) : Int = 
      try {from.toInt}
      catch{ case ex : Exception => throw new IllegalArgumentException("The value " + from + " is not a valid Integer.") }
  }
  
  implicit object ContainerTypeReader extends Reader[ContainerType, String]{
    def read(from:String) : ContainerType = {
      from.toLowerCase.trim match{
      	case "full" 	=> FullType
      	case "summary" 	=> SummaryType
      	case "link"	  	=> LinkType
      	case _ => throw new IllegalArgumentException("Cannot parse '" + from + "' to a known detail type. " + "Acceptable values are: 'full', 'summary', 'link'.")
      }
    }
  }
}
