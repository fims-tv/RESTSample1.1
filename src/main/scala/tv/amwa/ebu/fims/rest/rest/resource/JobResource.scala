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

package tv.amwa.ebu.fims.rest.rest.resource
import tv.amwa.ebu.fims.rest.commons.Loggable
import tv.amwa.ebu.fims.rest.engine.ServiceEngine
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobType
import tv.amwa.ebu.fims.rest.model.ResourceID
import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.applyDetail
import tv.amwa.ebu.fims.rest.rest.handling.RequestHandling
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import tv.amwa.ebu.fims.rest.model.ManageJobRequest
import tv.amwa.ebu.fims.rest.model.Pause
import tv.amwa.ebu.fims.rest.model.ModifyPriority
import tv.amwa.ebu.fims.rest.model.Resume
import tv.amwa.ebu.fims.rest.model.Restart
import tv.amwa.ebu.fims.rest.model.Cancel
import tv.amwa.ebu.fims.rest.model.Stop
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureJob
import tv.amwa.ebu.fims.rest.model.CleanUp
import tv.amwa.ebu.fims.rest.engine.NotFoundException
import tv.amwa.ebu.fims.rest.model.transfer.TransferJob
import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.model.transfer.TransferJobParameters
import tv.amwa.ebu.fims.rest.model.transform.TransformJob
import tv.amwa.ebu.fims.rest.model.transform.TransformJobParameters
import scala.reflect.runtime.universe._
import tv.amwa.ebu.fims.rest.model.transfer.TransferJobType
import tv.amwa.ebu.fims.rest.model.transform.TransformJobType
import tv.amwa.ebu.fims.rest.model.StartProcessType
import tv.amwa.ebu.fims.rest.model.BaseJobParameters
import tv.amwa.ebu.fims.rest.model.capture.CaptureJobParameters
import tv.amwa.ebu.fims.rest.model.StopProcessType
import tv.amwa.ebu.fims.rest.model.ManageJobRequestType
import javax.ws.rs.PUT


class JobResource[J <: JobType](val jobEngine: ServiceEngine[J], val resourceID: ResourceID)(implicit t: TypeTag[J]) extends RequestHandling with Loggable {
  import tv.amwa.ebu.fims.rest.rest.converter.ItemConverters.applyDetail
  
  @GET
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def getJob(@Context uriInfo: UriInfo): Response = httpOK(applyDetail(jobEngine.getResource(resourceID), uriInfo))

  @POST
  @Consumes(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def updateJob(jobDetails: JobType, @Context uriInfo: UriInfo): Response = 
    httpOK(applyDetail(jobEngine.updateResource(resourceID, jobDetails.asInstanceOf[J]), uriInfo))
  
  @DELETE
  def deleteJob: Response = httpNoContent(jobEngine.delete(resourceID))
  
  @POST
  @Path("manage")
  @Consumes(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def postJobCommand(jobCommand: ManageJobRequestType, @Context uriInfo: UriInfo) = {
    jobCommand.jobCommand match {
      case Pause => httpAsync(applyDetail(jobEngine.pause(resourceID), uriInfo))
      case Resume => httpAsync(applyDetail(jobEngine.resume(resourceID), uriInfo))
      case Restart => httpAsync(applyDetail(jobEngine.restart(resourceID), uriInfo))
      case Cancel => httpAsync(applyDetail(jobEngine.cancel(resourceID), uriInfo))
      case Stop => httpAsync(applyDetail(jobEngine.stop(resourceID), uriInfo))
      case ModifyPriority => 
        val updatedJob = t.tpe match {
          case t if t <:< typeOf[CaptureJobType] => CaptureJob(ResourceParameters(resourceID), BaseJobParameters(priority = jobCommand.priority), CaptureJobParameters())
          case t if t <:< typeOf[TransferJobType] => TransferJob(ResourceParameters(resourceID), BaseJobParameters(priority = jobCommand.priority), TransferJobParameters())
          case t if t <:< typeOf[TransformJobType] => TransformJob(ResourceParameters(resourceID), BaseJobParameters(priority = jobCommand.priority), TransformJobParameters())
          case _ => throw new IllegalArgumentException("Unsupported job type.")
        }
        httpAsync(applyDetail(jobEngine.updateResource(resourceID, updatedJob.asInstanceOf[J]), uriInfo))
      case CleanUp => 
        deleteJob
    }
  }
  
  @GET
  @Path("manage")
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def getJobStatus() = httpOK(jobEngine.getResource(resourceID).fold(e => Left(e), job => job match {
    case CaptureJob(rp, bp, sp) => Right(CaptureJob(rp, BaseJobParameters(status = bp.status), CaptureJobParameters()))
    case TransferJob(rp, bp, sp) => Right(TransferJob(rp, BaseJobParameters(status = bp.status), TransferJobParameters()))
    case TransformJob(rp, bp, sp) => Right(TransformJob(rp, BaseJobParameters(status = bp.status), TransformJobParameters()))
    case _ => Left(new IllegalArgumentException("Unsupported job type.")) }))
    
  // Non-FIMS specific sub-resource extensions for altering start and stop parameters
  @PUT
  @Path("startProcess")
  @Consumes(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def changeStartProcess(startProcess: StartProcessType, @Context uriInfo: UriInfo) = 
    httpOK(applyDetail(jobEngine.updateResource(resourceID, 
        CaptureJob(ResourceParameters(resourceID), BaseJobParameters(), 
            CaptureJobParameters(startProcess = Some(startProcess))).asInstanceOf[J]), uriInfo))
            
  @PUT
  @Path("stopProcess")
  @Consumes(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON))
  def changeStartProcess(stopProcess: StopProcessType, @Context uriInfo: UriInfo) = 
    httpOK(applyDetail(jobEngine.updateResource(resourceID, 
        CaptureJob(ResourceParameters(resourceID), BaseJobParameters(), 
            CaptureJobParameters(stopProcess = Some(stopProcess))).asInstanceOf[J]), uriInfo))

}
