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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, AskPriceQuotingPolicy}


/** Trait that extends "auction-like" behavior to include the ability to process ask price quote requests.
  *
  * @tparam T all `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidAuctionLike[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }]
  extends SealedBidAuctionLike[T, A] with AskPriceQuoting[T, A] {

  protected val askPriceQuotingPolicy: AskPriceQuotingPolicy[T] = new AskPriceQuotingPolicy[T]

}


/** Companion object for the `OpenBidDoubleAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidAuctionLike {

  class Ops[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: OpenBidAuctionLike[T, A]) {

    /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
      *
      * @param order the `BidOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances.
      */
    def insert(order: BidOrder[T]): A = ev.insert(a, order)

    def receive(request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = ev.receive(a, request)

    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

  }

}
