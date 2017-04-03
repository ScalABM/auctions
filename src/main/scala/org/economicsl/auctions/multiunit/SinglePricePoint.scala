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
package org.economicsl.auctions.multiunit

import org.economicsl.auctions._

import scala.collection.immutable


/** Mixin trait defining an `Order` for multiple units of a `Tradable` at some limit price. */
trait SinglePricePoint[+T <: Tradable] extends PriceQuantitySchedule[T] {
  this: Order[T] =>

  /** Limit price (per unit of the `Tradable`) for the Order.
    *
    * The limit price imposes an upper (lower) bound on a bid (ask) order.
    */
  def limit: Price

  /** Desired number of units of the `Tradable`. */
  def quantity: Quantity

  val schedule: immutable.Map[Price, Quantity] = immutable.Map(limit -> quantity)

  /** The total value of the order */
  val value: Currency = limit.value * quantity.value

}


/** Companion object for the `SinglePricePoint` trait.
  *
  * Defines a basic ordering for any `Order` that mixes in the `SinglePricePoint` trait.
  */
object SinglePricePoint {

  /** All `Order` instances that mixin `SinglePricePoint` are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `Order with SinglePricePoint` that is being ordered.
    * @return an `Ordering` defined over `Order with SinglePricePoint` instances.
    */
  def ordering[O <: Order[_ <: Tradable] with SinglePricePoint[_ <: Tradable]]: Ordering[O] = {
    Ordering.by(o => (o.limit, o.issuer))
  }

}

