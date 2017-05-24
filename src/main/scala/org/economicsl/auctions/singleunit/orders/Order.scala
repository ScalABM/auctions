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
package org.economicsl.auctions.singleunit.orders

import org.economicsl.auctions.{Contract, OrderLike, SingleUnit, Tradable}


/** Base trait for all single-unit orders.
  *
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait Order[+T <: Tradable] extends Contract with OrderLike[T] with SingleUnit[T]


/** Companion object for the `Order` trait.
  *
  * Defines a basic ordering for anything that mixes in the `Order` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object Order {

  /** All `Order` instances are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `Order` that is being ordered.
    * @return an `Ordering` defined over `Order` instances.
    */
  implicit def ordering[O <: Order[_ <: Tradable]]: Ordering[O] = {
    SingleUnit.ordering
  }

}


/** Base trait for all single-unit orders to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `AskOrder` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
trait AskOrder[+T <: Tradable] extends Order[T]


/** Base trait for all single-unit orders to buy a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `BidOrder` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
trait BidOrder[+T <: Tradable] extends Order[T]