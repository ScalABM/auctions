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
package org.economicsl.auctions.multiunit.orders

import org.economicsl.auctions.{Issuer, Order, SinglePricePoint}
import org.economicsl.core.{Price, Quantity, Tradable}


/** Base trait for all single price-point order implementations.
  *
  * @tparam T
  */
sealed trait SinglePricePointOrder[+T <: Tradable] extends Order[T] with SinglePricePoint[T]


/** Companion object for the `SinglePricePointOrder` trait.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointOrder {

  /** All `SinglePricePointOrder` instances are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `SinglePricePointOrder` that is being ordered.
    * @return `Ordering` defined over `SinglePricePointOrder` instances.
    */
  def ordering[T <: Tradable, O <: SinglePricePointOrder[T]]: Ordering[O] = {
    Ordering.by(o => (o.limit, o.issuer)) // todo re-visit whether or not issuer can only have a single active order!
  }

}


/** Base trait for all multi-unit orders to sell a particular `Tradable`.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
case class SinglePricePointAskOrder[+T <: Tradable](
  issuer: Issuer,
  limit: Price,
  quantity: Quantity,
  tradable: T)
    extends SinglePricePointOrder[T]


/** Companion object for SinglePricePointBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointAskOrder {

  def apply[T <: Tradable](issuer: Issuer, quantity: Quantity, tradable: T): SinglePricePointAskOrder[T] = {
    new SinglePricePointAskOrder(issuer, Price.MinValue, quantity, tradable)
  }

}


/** Class representing a multi-unit orders to buy a particular `Tradable`
  *
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SinglePricePointBidOrder[+T <: Tradable](
  issuer: Issuer,
  limit: Price,
  quantity: Quantity,
  tradable: T)
    extends SinglePricePointOrder[T]


/** Companion object for SinglePricePointBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointBidOrder {

  def apply[T <: Tradable](issuer: Issuer, quantity: Quantity, tradable: T): SinglePricePointBidOrder[T] = {
    new SinglePricePointBidOrder[T](issuer, Price.MaxValue, quantity, tradable)
  }

}