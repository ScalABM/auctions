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
package org.economicsl.auctions.singleunit.quotes

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook


/** Base trait for all quote policies. */
sealed trait QuotePolicy[T <: Tradable, -R <: QuoteRequest, +Q <: Quote] extends ((FourHeapOrderBook[T], R) => Option[Q])


class PriceQuotePolicy[T <: Tradable] extends QuotePolicy[T, PriceQuoteRequest, PriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: PriceQuoteRequest): Option[PriceQuote] = request match {
    case _: AskPriceQuoteRequest => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
    case _: BidPriceQuoteRequest => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
    case _: SpreadQuoteRequest => orderBook.spread.map(quote => SpreadQuote(quote))
    case _ => None
  }

}


