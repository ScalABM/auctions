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

import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


/**Documentation for the Single Unit Auctions goes here! */
package object singleunit {

  class AuctionLikeOps[T <: Tradable, A](a: A)(implicit ev: AuctionLike[T, A]) {

    def insert(order: BidOrder[T]): A = ev.insert(a, order)

    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

    protected val orderBook: FourHeapOrderBook[T] = ev.orderBook(a)

    protected val pricingPolicy: PricingPolicy[T] = ev.pricingPolicy(a)

  }

  class OpenAuctionLikeOps[T <: Tradable, A](a: A)(implicit ev: OpenAuctionLike[T, A])
    extends AuctionLikeOps(a)(ev) {

    def receive(request: BidPriceQuoteRequest): Option[BidPriceQuote] = ev.receive(a, request)

  }

}
