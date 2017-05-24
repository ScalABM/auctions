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
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenReverseAuctionLike[T <: Tradable, A] extends ReverseAuctionLike[T, A] with BidPriceQuoting[T, A] {

  protected val bidPriceQuotingPolicy: BidPriceQuotingPolicy[T] = new BidPriceQuotingPolicy[T]

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenReverseAuctionLike {

  class Ops[T <: Tradable, A](a: A)(implicit ev: OpenReverseAuctionLike[T, A]) {

    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def receive(request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = ev.receive(a, request)

    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

    protected val orderBook: FourHeapOrderBook[T] = ev.orderBook(a)

    protected val pricingPolicy: PricingPolicy[T] = ev.pricingPolicy(a)

  }

}
