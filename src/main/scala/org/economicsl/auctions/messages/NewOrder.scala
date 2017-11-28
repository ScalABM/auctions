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

import org.economicsl.core.Tradable


/** Base trait  message sent from an `AuctionParticipant` to an `Auction` specifying a new order.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait NewOrder[+T <: Tradable] extends Message {
  this: PriceQuantitySchedule[T] =>

  /* Unique identifier for an order assigned by an `AuctionParticipant`. */
  def orderId: OrderId

}


/** Companion object for the `NewOrder` trait.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object NewOrder {

  /** Defines an ordering over `NewOrder` types.
    *
    * @tparam O subtype of `NewOrder` for which the ordering is defined.
    * @return an ordering over a subtype of `NewOrder`.
    */
  def ordering[O <: NewOrder[_<: Tradable]]: Ordering[O] = {
    Ordering.by(order => (order.senderId, order.orderId))
  }

}





