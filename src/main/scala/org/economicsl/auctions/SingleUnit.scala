/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions


/** Mixin trait that restricts the quantity of an `Order` to a single unit of a `Tradable`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SingleUnit[+T <: Tradable] extends SinglePricePoint[T] {
  this: Contract with OrderLike[T] =>

  val quantity = Quantity(1)

}


/** Companion object for the `SingleUnit` trait.
  *
  * Defines a basic ordering for anything that mixes in the `SingleUnit` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SingleUnit {

  /** All `Contract with OrderLike` instances that mixin `SingleUnit` are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `Contract with OrderLike[_] with SingleUnit[_]` that is being ordered.
    * @return `Ordering` defined over `Contract with OrderLike[_] with SingleUnit[_]` instances.
    */
  def ordering[O <: Contract with OrderLike[_ <: Tradable] with SingleUnit[_ <: Tradable]]: Ordering[O] = {
    SinglePricePoint.ordering
  }

}