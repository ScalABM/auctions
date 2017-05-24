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
package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.{ClearResult, OpenAuctionLike}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, BidPriceQuoting, SpreadQuoting, SpreadQuotingPolicy}
import org.economicsl.auctions.singleunit.reverse.OpenReverseAuctionLike


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenDoubleAuctionLike[T <: Tradable, A] extends OpenAuctionLike[T, A] with OpenReverseAuctionLike[T, A]
  with AskPriceQuoting[T, A] with BidPriceQuoting[T, A] with SpreadQuoting[T, A] {

  protected val spreadQuotingPolicy: SpreadQuotingPolicy[T] = new SpreadQuotingPolicy[T]

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenDoubleAuctionLike {

  class Ops[T <: Tradable, A](a: A)(implicit ev: OpenDoubleAuctionLike[T, A]) {

    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def insert(order: BidOrder[T]): A = ev.insert(a, order)

    def receive(request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = ev.receive(a, request)

    def receive(request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = ev.receive(a, request)

    def receive(request: SpreadQuoteRequest[T]): Option[SpreadQuote] = ev.receive(a, request)

    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

  }

}
