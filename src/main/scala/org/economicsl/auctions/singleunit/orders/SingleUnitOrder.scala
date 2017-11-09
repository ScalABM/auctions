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

import org.economicsl.auctions.messages._
import org.economicsl.auctions.{IssuerId, Order}
import org.economicsl.core.{Price, Tradable}


/** Base trait for all `SingleUnitOrder` implementations.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait SingleUnitOrder[+T <: Tradable]
  extends Order[T]
    with SinglePricePoint[T]
    with SingleUnit[T]


/** Companion object for the `SingleUnitOrder` trait.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitOrder {

  /** All `SingleUnitOrder` instances are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `SingleUnitOrder` that is being ordered.
    * @return `Ordering` defined over `SingleUnitOrder` instances.
    */
  def ordering[T <: Tradable, O <: SingleUnitOrder[T]]: Ordering[O] = {
    Ordering.by(o => (o.limit, o.issuerId)) // todo re-visit whether or not issuer can only have a single active order!
  }

  def from[T <: Tradable](message: NewSingleUnitOrder[T]): SingleUnitOrder[T] = message match {
    case m: NewSingleUnitBid[T] => SingleUnitBid.from(m)
    case m: NewSingleUnitOffer[T] => SingleUnitOffer.from(m)
  }

}


/** Class representing a single-unit order to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `AskOrder` is being issued.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class SingleUnitOffer[+T <: Tradable](issuerId: IssuerId, limit: Price, tradable: T)
  extends SingleUnitOrder[T]


/** Companion object for `SingleUnitAskOrder`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitOffer {

  def apply[T <: Tradable](issuer: IssuerId, tradable: T): SingleUnitOffer[T] = {
    new SingleUnitOffer(issuer, Price.MinValue, tradable)
  }

  /** Factory method for creating `SingleUnitOffer` from a `NewSingleUnitOffer` message.
    *
    * @param message
    * @tparam T
    * @return
    */
  def from[T <: Tradable](message: NewSingleUnitOffer[T]): SingleUnitOffer[T] = {
    new SingleUnitOffer[T](message.senderId, message.limit, message.tradable)
  }

}


/** Class representing a single-unit orders to buy a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `BidOrder` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SingleUnitBid[+T <: Tradable](issuerId: IssuerId, limit: Price, tradable: T)
  extends SingleUnitOrder[T]


/** Companion object for SingleUnitBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitBid {

  def apply[T <: Tradable](issuer: IssuerId, tradable: T): SingleUnitBid[T] = {
    new SingleUnitBid(issuer, Price.MaxValue, tradable)
  }

  /** Factory method for creating `SingleUnitBid` from a `NewSingleUnitBid` message.
    *
    * @param message
    * @tparam T
    * @return
    */
  def from[T <: Tradable](message: NewSingleUnitBid[T]): SingleUnitBid[T] = {
    new SingleUnitBid[T](message.senderId, message.limit, message.tradable)
  }

}