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
package org.economicsl.auctions.messages

import org.economicsl.core.{Price, Quantity, Tradable}

import scala.collection.immutable


/** Mixin trait defining an `Order` for multiple units of a `Tradable` at some limit price.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SinglePricePoint[+T <: Tradable] extends PriceQuantitySchedule[T] {

  /** Limit price (per unit of the `Tradable`) for the Order.
    *
    * The limit price imposes an upper (lower) bound on a bid (ask) order.
    */
  def limit: Price

  /** Desired number of units of the `Tradable`. */
  def quantity: Quantity

  val schedule: immutable.Map[Price, Quantity] = immutable.Map(limit -> quantity)

}

