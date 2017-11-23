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
import org.economicsl.core.{Price, Tradable}


/** Base trait for all `NewSingleUnitOrder` messages.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait NewSingleUnitOrder[+T <: Tradable]
  extends NewSinglePricePointOrder[T]
  with SingleUnit[T]


/** Companion object for the `NewSingleUnitOrder` trait.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object NewSingleUnitOrder {

  /** An ordering of `NewSingleUnitOrder` instances based on limit price.
    *
    * @tparam T
    * @tparam O
    * @return an ordering of `NewSingleUnitOrder` instances based on limit price.
    */
  def priceOrdering[T <: Tradable, O <: NewSingleUnitOrder[T]]: Ordering[O] = {
    NewSinglePricePointOrder.priceOrdering[T, O]
  }

}


final case class NewSingleUnitBid[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSingleUnitOrder[T] {

  /** A `NewSingleUnitBid` is a `NewSingleUnitOrder` to buy a particular type of `Tradable`. */
  val side: Char = '1'

}


/** Companion object for the `NewSingleUnitOrder` trait.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object NewSingleUnitBid {

  /** An ordering of `NewSingleUnitBid` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `NewSingleUnitBid` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[NewSingleUnitBid[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, NewSingleUnitBid[T]]
  }

}


final case class NewSingleUnitOffer[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSingleUnitOrder[T] {

  /** A `NewSingleUnitOffer` is a `NewSingleUnitOrder` to sell a particular type of `Tradable`. */
  val side: Char = '2'

}


/** Companion object for the `NewSingleUnitOffer` trait.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object NewSingleUnitOffer {

  /** An ordering of `NewSingleUnitOffer` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `NewSingleUnitOffer` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[NewSingleUnitOffer[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, NewSingleUnitOffer[T]]
  }

}