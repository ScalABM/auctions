/*
Copyright 2017 EconomicSL

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
sealed trait QuotePolicy[T <: Tradable] {
  //this: DoubleAuction[T] =>   // really I just want to to be something that has an orderBook!

  def receive: PartialFunction[QuoteRequest, Option[Quote]]

  def orderBook: FourHeapOrderBook[T] // todo consider creating a mixin?

}


trait AskQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def receive: PartialFunction[QuoteRequest, Option[Quote]] = {
    case AskPriceQuoteRequest => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
  }

}


trait BidQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def receive: PartialFunction[QuoteRequest, Option[Quote]] = {
    case BidPriceQuoteRequest => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
  }

}


trait SpreadQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def receive: PartialFunction[QuoteRequest, Option[Quote]] = {
    case SpreadQuoteRequest => orderBook.spread.map(quote => SpreadQuote(quote))
  }

}


trait BasicQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def receive: PartialFunction[QuoteRequest, Option[Quote]] = {
    case AskPriceQuoteRequest => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
    case BidPriceQuoteRequest => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
    case SpreadQuoteRequest => orderBook.spread.map(quote => SpreadQuote(quote))
  }

}


