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
import org.economicsl.auctions.singleunit.DoubleAuction


/** Base trait for all quote policies. */
sealed trait QuotePolicy[T <: Tradable] extends PartialFunction[QuoteRequest, Option[Quote]]


/** A sealed-bid quote policy means that auction participants cannot obtain information about the current bid quote. */
class SealedBidPolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = false

  def apply(request: QuoteRequest): Option[Quote] = None

}


/** A sealed-bid quote policy means that auction participants cannot obtain information about the current ask quote. */
class SealedAskPolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = false

  def apply(request: QuoteRequest): Option[Quote] = None

}


class AskQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = {
    case AskPriceQuoteRequest => true
    case _ => false
  }

  def apply: PartialFunction[QuoteRequest, Option[Quote]] = {
    case AskPriceQuoteRequest => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
  }

}


class BidQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = {
    case BidPriceQuoteRequest => true
    case _ => false
  }

  def apply: PartialFunction[QuoteRequest, Option[Quote]] = {
    case BidPriceQuoteRequest => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
  }

}


class SpreadQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = {
    case SpreadQuoteRequest => true
    case _ => false
  }

  def apply: PartialFunction[QuoteRequest, Option[Quote]] = {
    case SpreadQuoteRequest => orderBook.spread.map(quote => SpreadQuote(quote))
  }

}


trait BasicQuotePolicy[T <: Tradable] extends QuotePolicy[T] {

  def isDefinedAt(request: QuoteRequest): Boolean = {
    case AskPriceQuoteRequest => true
    case BidPriceQuoteRequest => true
    case SpreadQuoteRequest => true
    case _ => false
  }

  def apply(request: QuoteRequest): Option[Quote] = {
    case AskPriceQuoteRequest => orderBook.askPriceQuote.map(quote => AskPriceQuote(quote))
    case BidPriceQuoteRequest => orderBook.bidPriceQuote.map(quote => BidPriceQuote(quote))
    case SpreadQuoteRequest => orderBook.spread.map(quote => SpreadQuote(quote))
  }

}


