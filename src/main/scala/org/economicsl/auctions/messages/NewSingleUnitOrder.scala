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


final case class SingleUnitBid[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSingleUnitOrder[T] {

  /** A `SingleUnitBid` is a `NewSingleUnitOrder` to buy a particular type of `Tradable`. */
  val side: Char = '1'

}


/** Companion object for the `SingleUnitBid` class.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object SingleUnitBid {

  /** An ordering of `SingleUnitBid` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `SingleUnitBid` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[SingleUnitBid[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, SingleUnitBid[T]]
  }

}


final case class SingleUnitOffer[+T <: Tradable](
  limit: Price,
  orderId: OrderId,
  senderId: SenderId,
  timestamp: Timestamp,
  tradable: T)
    extends NewSingleUnitOrder[T] {

  /** A `SingleUnitOffer` is a `NewSingleUnitOrder` to sell a particular type of `Tradable`. */
  val side: Char = '2'

}


/** Companion object for the `SingleUnitOffer` trait.
  *
  * @author davidrpugh
  * @ since 0.2.0
  */
object SingleUnitOffer {

  /** An ordering of `SingleUnitOffer` instances based on limit price.
    *
    * @tparam T
    * @return an ordering of `SingleUnitOffer` instances based on limit price.
    */
  def priceOrdering[T <: Tradable]: Ordering[SingleUnitOffer[T]] = {
    NewSinglePricePointOrder.priceOrdering[T, SingleUnitOffer[T]]
  }

}