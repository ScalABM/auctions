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

import org.economicsl.auctions.messages._
import org.economicsl.auctions.{IssuerId, Order}
import org.economicsl.core.{Price, Quantity, Tradable}


/** Base trait for all single price-point order implementations.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait SinglePricePointOrder[+T <: Tradable]
  extends Order[T]
    with SinglePricePoint[T]


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
    Ordering.by(o => (o.limit, o.issuerId)) // todo re-visit whether or not issuer can only have a single active order!
  }

  def from[T <: Tradable](message: NewSinglePricePointOrder[T]): SinglePricePointOrder[T] = message match {
    case m: NewSinglePricePointBid[T] => SinglePricePointBid.from(m)
    case m: NewSinglePricePointOffer[T] => SinglePricePointOffer.from(m)
  }

}


/** Class representing a multi-unit orders to buy a particular `Tradable`
  *
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SinglePricePointBid[+T <: Tradable](
  issuerId: IssuerId,
  limit: Price,
  quantity: Quantity,
  tradable: T)
    extends SinglePricePointOrder[T]


/** Companion object for SinglePricePointBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointBid {

  def apply[T <: Tradable](issuer: IssuerId, quantity: Quantity, tradable: T): SinglePricePointBid[T] = {
    new SinglePricePointBid[T](issuer, Price.MaxValue, quantity, tradable)
  }

  def from[T <: Tradable](message: NewSinglePricePointBid[T]): SinglePricePointBid[T] = {
    new SinglePricePointBid[T](message.orderId, message.limit, message.quantity, message.tradable)
  }

}


/** Base trait for all multi-unit orders to sell a particular `Tradable`.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
case class SinglePricePointOffer[+T <: Tradable](
  issuerId: IssuerId,
  limit: Price,
  quantity: Quantity,
  tradable: T)
    extends SinglePricePointOrder[T]


/** Companion object for SinglePricePointBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointOffer {

  def apply[T <: Tradable](issuer: IssuerId, quantity: Quantity, tradable: T): SinglePricePointOffer[T] = {
    new SinglePricePointOffer(issuer, Price.MinValue, quantity, tradable)
  }

  def from[T <: Tradable](message: NewSinglePricePointOffer[T]): SinglePricePointOffer[T] = {
    new SinglePricePointOffer[T](message.orderId, message.limit, message.quantity, message.tradable)
  }

}