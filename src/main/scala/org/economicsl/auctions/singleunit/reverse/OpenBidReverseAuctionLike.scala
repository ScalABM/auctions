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
package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.ClearResult
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}


/** Trait extends sealed-bid reverse auction-like behavior to include the ability to process bid price quote requests.
  *
  * @tparam T all `AskOrder` instances must be for the same type of `Tradable`.
  * @tparam A type class `A` for which sealed-bid reverse auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidReverseAuctionLike[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }]
  extends SealedBidReverseAuctionLike[T, A]
  with BidPriceQuoting[T, A] {

  /* Defines how an `OpenBidReverseAuctionLike` instance will respond to `BidPriceQuoteRequests`. */
  protected val bidPriceQuotingPolicy: BidPriceQuotingPolicy[T] = new BidPriceQuotingPolicy[T]

}


/** Companion object for the `OpenBidReverseAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidReverseAuctionLike {

  class Ops[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: OpenBidReverseAuctionLike[T, A]) {

    /** Create a new instance of type class `A` whose order book contains an additional `AskOrder`.
      *
      * @param order the `AskOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
      */
    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def receive(request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = ev.receive(a, request)

    /** Create a new instance of type class `A` whose order book contains all previously submitted `AskOrder` instances
      * except the `order`.
      *
      * @param order the `AskOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances
      *         except the `order`.
      */
    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[T, A] = ev.clear(a)

  }

}
