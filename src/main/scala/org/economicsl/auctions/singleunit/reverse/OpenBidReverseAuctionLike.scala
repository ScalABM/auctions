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

import org.economicsl.auctions.{ClearResult, Reference, Token}
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}
import org.economicsl.core.Tradable





/** Companion object for the `OpenBidReverseAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidReverseAuctionLike {

  class Ops[T <: Tradable, A <: Auction[T]](a: A)(implicit ev: OpenBidReverseAuctionLike[T, A]) {

    import org.economicsl.auctions.singleunit.AuctionParticipant._

    /** Remove a previously accepted `AskOrder` instance from the `orderBook`.
      *
      * @param reference the unique identifier for the `AskOrder` that should be removed from the `orderBook`.
      * @return a `Tuple` whose first element is a `Canceled` instance encapsulating information about `AskOrder` that
      *         has been removed from the `orderBook` and whose second element is an instance of type class `A` whose
      *         `orderBook` contains all previously submitted `AskOrder` instances except the `AskOrder` corresponding
      *         to the `reference` identifier.
      */
    def cancel(reference: Reference): (A, Option[Canceled]) = ev.cancel(a, reference)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[A] = ev.clear(a)

    /** Create a new instance of type class `A` whose order book contains an additional `AskOrder`.
      *
      * @param kv
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
      */
    def insert(kv: (Token, Order[T])): (A, Either[Rejected, Accepted]) = ev.insert(a, kv)

    def receive(request: BidPriceQuoteRequest[T]): BidPriceQuote = ev.receive(a, request)

  }

}
