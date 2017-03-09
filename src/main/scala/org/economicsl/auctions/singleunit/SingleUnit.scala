/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{Order, Quantity, Tradable, multiunit}


/** Mixin trait that restricts the quantity of an `Order with SinglePricePoint` to a single unit of a `Tradable`. */
trait SingleUnit[+T <: Tradable] extends multiunit.SinglePricePoint[T] {
  this: Order[T]  =>

  val quantity = Quantity(1)

}


/** Companion object for the `SingleUnit` trait.
  *
  * Defines a basic ordering for anything that mixes in the `SingleUnit` trait.
  */
object SingleUnit {

  /** All `Order` instances that mixin `SingleUnit` are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `Order with SinglePricePoint` that is being ordered.
    * @return and `Ordering` defined over `Order with SinglePricePoint` instances.
    */
  def ordering[O <: Order[_ <: Tradable] with SingleUnit[_ <: Tradable]]: Ordering[O] = {
    multiunit.SinglePricePoint.ordering
  }

}

