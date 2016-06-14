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

package tv.amwa.ebu.fims.rest.model.transfer

import tv.amwa.ebu.fims.rest.model.JobType
import tv.amwa.ebu.fims.rest.model.ServiceJobParameters
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.BaseJobParameters

trait TransferJobType extends JobType {
  val serviceParameters: TransferJobParameters
  lazy val profiles = serviceParameters.profiles
  override def checkOnCreation = {
    super.checkOnCreation
    require(profiles.length > 0, "A transfer job request must contain at least one profile.")
  }
}

case class TransferJobParameters(
    profiles: Seq[TransferProfileType] = Nil) extends ServiceJobParameters
    
case class TransferJob(
    resourceParameters: ResourceParameters,
    baseParameters: BaseJobParameters,
    serviceParameters: TransferJobParameters) extends TransferJobType