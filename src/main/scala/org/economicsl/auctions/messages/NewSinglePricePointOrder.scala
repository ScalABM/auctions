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

import org.economicsl.core.util.Timestamp
import org.economicsl.core.{Price, Quantity, Tradable}


/** Base trait for all `NewSinglePricePointOrder` messages.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait NewSinglePricePointOrder[+T <: Tradable]
  extends NewOrder[T]
  with SinglePricePoint[T] {

  /** Side of the `NewOrder`.
    *
    * @note Current valid values are 1: "buy" and 2: "sell".
    */
  def side: Char

}


/** Companion object for the `NewSinglePricePointOrder` trait.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object NewSinglePricePointOrder {

  /** An ordering of `NewSinglePricePointOrder` instances based on limit price.
    *
    * @tparam T
    * @tparam O
    * @return an ordering of `NewSinglePricePointOrder` instances based on limit price.
    */
  def priceOrdering[T <: Tradable, O <: NewSinglePricePointOrder[T]]: Ordering[O] = {
    Ordering.by(order => (order.limit, order.senderId, order.orderId))
  }

}


final case class NewSinglePricePointBid[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  quantity: Quantity,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSinglePricePointOrder[T] {

  /** A NewSinglePricePointBid` is a `NewSinglePricePointOrder` to buy a particular type of `Tradable`. */
  val side: Char = '1'

}


/** Companion object for `NewSinglePricePointBid`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object NewSinglePricePointBid {

  /** An ordering of `NewSinglePricePointBid` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `NewSinglePricePointBid` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[NewSinglePricePointBid[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, NewSinglePricePointBid[T]]
  }

}


final case class NewSinglePricePointOffer[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  quantity: Quantity,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSinglePricePointOrder[T] {

  /** A `NewSinglePricePointOffer` is a `NewSinglePricePointOrder` to sell a particular type of `Tradable`. */
  val side: Char = '2'

}


/** Companion object for `NewSinglePricePointOffer`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object NewSinglePricePointOffer {

  /** An ordering of `NewSinglePricePointOffer` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `NewSinglePricePointOffer` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[NewSinglePricePointOffer[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, NewSinglePricePointOffer[T]]
  }

}