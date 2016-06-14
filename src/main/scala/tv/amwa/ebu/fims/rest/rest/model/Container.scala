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

package tv.amwa.ebu.fims.rest.rest.model
import tv.amwa.ebu.fims.rest.converter.Writer
import tv.amwa.ebu.fims.rest.model.ResourceType
import tv.amwa.ebu.fims.rest.converter.FimsString

sealed abstract class ContainerType(val detail: String) {
  override def toString() = detail
}
case object LinkType extends ContainerType("reference")
case object SummaryType extends ContainerType("summary")
case object FullType extends ContainerType("full")

case class Link(href: String, rel: Option[String])

sealed trait Item[T] {
  val value: T
}

case class LinkItem[T](value:T, link: Link) extends Item[T]
case class SummaryItem[T](value: T, link: Link) extends Item[T]
case class FullItem[T](value: T, link: Link) extends Item[T]

class Container[T, M[_] <: Item[_]](val totalSize: Int, val skip: Option[Int], val limit: Option[Int], seeds: List[T])(implicit t: Writer[T, M[T]]) {
  val items: List[M[T]] = seeds.map { v => t.write(v) }
  def page: Option[Int] = for (s <- skip; l <- limit) yield (s / l) + 1
  def pages: Option[Int] = for (s <- skip; l <- limit) yield (totalSize / l) + 1
}