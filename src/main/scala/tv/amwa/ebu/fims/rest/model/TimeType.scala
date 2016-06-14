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

package tv.amwa.ebu.fims.rest.model

sealed trait TimeSpecifier

case class Timecode(
    val timecode: String) extends TimeSpecifier {
  val timecodePattern = """[0-9][0-9]\:[0-5][0-9]\:[0-5][0-9][\:\;][0-9][0-9](\.[0-1])?"""
  require(timecode.matches(timecodePattern))
}
    
case class NormalPlayTime(
    val duration: Long) extends TimeSpecifier {
  require(duration >= 0, "Cannot specify a negative duration.")
}
    
case class EditUnitNumber(
    val editUnit: Long,
    val editRate: Int,
    val factorNumerator: Int,
    val factorDenominator: Int) extends TimeSpecifier {
  require(factorDenominator > 0, "Factor denominator for an edit unit number must be a postive number.")
  require(factorNumerator > 0, "Factor numerator for an edit unit number must be a postive number.")
  require(editRate > 0, "Edit rate for an edit unit number must be a postive number.")
}

/** Value used to represent a point in time, such as at what time to start an operation.
  */
case class Time(
    val specifier: TimeSpecifier)

case class Duration(
	 val specifier: TimeSpecifier)