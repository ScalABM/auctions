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
package org.economicsl.auctions

import org.economicsl.auctions.messages.{InvalidTickSize, NewOrderAccepted, NewOrderRejected, NewSingleUnitOrder}
import org.economicsl.core.Tradable


trait GenSingleUnitAuction[T <: Tradable, OB <: OrderBook[T, NewSingleUnitOrder[T], OB], A <: GenSingleUnitAuction[T, OB, A]]
    extends GenAuction[T, NewSingleUnitOrder[T], OB, A] {
  this: A =>

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param message
    * @return
    */
  override def insert(message: NewSingleUnitOrder[T]): (A, Either[NewOrderRejected, NewOrderAccepted]) = {
    if (message.limit.value % protocol.tickSize > 0) {
      val timestamp = currentTimeMillis()
      val reason = InvalidTickSize(message.limit, protocol.tickSize)
      val rejected = NewOrderRejected(message.orderId, reason, auctionId, timestamp)
      (this, Left(rejected))
    } else {
      super.insert(message)
    }
  }

  /** Combines and `Auction` mechanism with some other `Auction`.
    *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: A): A = {
    require(protocol.tradable.equals(that.protocol.tradable), "Only auctions for the same Tradable can be combined!")
    val combinedOrderBooks = orderBook.combineWith(that.orderBook)
    val withCombinedOrderBooks = withOrderBook(combinedOrderBooks)
    val combinedTickSize = leastCommonMultiple(protocol.tickSize, that.protocol.tickSize)
    val updatedProtocol = protocol.withTickSize(combinedTickSize)
    withCombinedOrderBooks.withProtocol(updatedProtocol)
  }

}
