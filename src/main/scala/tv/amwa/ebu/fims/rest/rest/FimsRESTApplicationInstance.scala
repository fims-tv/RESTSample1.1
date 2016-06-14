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

package tv.amwa.ebu.fims.rest.rest
import org.mortbay.jetty.servlet.ServletHolder
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context

abstract class FimsRESTApplicationInstance(servletHolder: ServletHolder, port: Int) {
  servletHolder.setInitParameter("javax.ws.rs.Application", classOf[FimsRESTApplication].getName)
  val server = new Server(port)
  val context = new Context(server, "/api", Context.SESSIONS)
  context.addServlet(servletHolder, "/*")

  def start = server.start
  def stop 	= server.stop
}