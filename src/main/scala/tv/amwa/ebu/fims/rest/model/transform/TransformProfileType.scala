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

package tv.amwa.ebu.fims.rest.model.transform

import tv.amwa.ebu.fims.rest.model.ProfileType
import tv.amwa.ebu.fims.rest.model.TransformAtom
import tv.amwa.ebu.fims.rest.model.TransferAtom
import tv.amwa.ebu.fims.rest.model.ServiceProfileParameters
import tv.amwa.ebu.fims.rest.model.ResourceParameters
import tv.amwa.ebu.fims.rest.model.BaseProfileParameters

trait TransformProfileType extends ProfileType {
  val serviceParameters: TransformProfileParameters
  lazy val transformAtom = serviceParameters.transformAtom
  lazy val transferAtom = serviceParameters.transferAtom
  lazy val outputFileNamePattern = serviceParameters.outputFileNamePattern
  def checkOnRequest = {
    require(transformAtom.isDefined)
    require(transferAtom.size > 0)
  }
}

case class TransformProfile(
    resourceParameters: ResourceParameters,
    baseParameters: BaseProfileParameters,
    serviceParameters: TransformProfileParameters) extends TransformProfileType

case class TransformProfileParameters(
    transformAtom: Option[TransformAtom] = None,
    transferAtom: Seq[TransferAtom] = Nil,
    outputFileNamePattern: Option[String] = None) extends ServiceProfileParameters