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
package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait QuotingPolicy[T <: Tradable, -R <: QuoteRequest[T], +Q <: Quote] extends ((FourHeapOrderBook[T], R) => Option[Q])


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class AskPriceQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, AskPriceQuoteRequest[T], AskPriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = {
    orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class BidPriceQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, BidPriceQuoteRequest[T], BidPriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = {
    orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class PriceQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, PriceQuoteRequest[T], PriceQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: PriceQuoteRequest[T]): Option[PriceQuote] = request match {
    case _: AskPriceQuoteRequest[T] => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
    case _: BidPriceQuoteRequest[T] => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
    case _: SpreadQuoteRequest[T] => orderBook.spread.map(quote => SpreadQuote(quote))
    case _ => None
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SpreadQuotingPolicy[T <: Tradable] extends QuotingPolicy[T, SpreadQuoteRequest[T], SpreadQuote] {

  def apply(orderBook: FourHeapOrderBook[T], request: SpreadQuoteRequest[T]): Option[SpreadQuote] = {
    orderBook.spread.map(quote => SpreadQuote(quote))
  }

}