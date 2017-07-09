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

import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.core.Tradable


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait QuotingPolicy[T <: Tradable, -R <: QuoteRequest[T], +Q <: Quote, A <: OpenBidAuction[T, R, A]] {
  this: A =>

  def receive(request: R): Q

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait AskPriceQuotingPolicy[T <: Tradable, A <: OpenBidAuction[T, AskPriceQuoteRequest[T], A]]
    extends QuotingPolicy[T, AskPriceQuoteRequest[T], AskPriceQuote, A] {
  this: A =>

  def receive(request: AskPriceQuoteRequest[T]): AskPriceQuote = {
    AskPriceQuote(request(orderBook))
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait BidPriceQuotingPolicy[T <: Tradable, A <: OpenBidAuction[T, BidPriceQuoteRequest[T], A]]
    extends QuotingPolicy[T, BidPriceQuoteRequest[T], BidPriceQuote, A] {
  this: A =>

  def receive(request: BidPriceQuoteRequest[T]): BidPriceQuote = {
    BidPriceQuote(request(orderBook))
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuotingPolicy[T <: Tradable, A <: OpenBidAuction[T, PriceQuoteRequest[T], A]]
  extends QuotingPolicy[T, PriceQuoteRequest[T], PriceQuote, A] {
  this: A =>

  def receive(request: PriceQuoteRequest[T]): PriceQuote = request match {
    case f: AskPriceQuoteRequest[T] =>  AskPriceQuote(f(orderBook))
    case f: BidPriceQuoteRequest[T] => BidPriceQuote(f(orderBook))
  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SpreadQuotingPolicy[T <: Tradable, A <: OpenBidAuction[T, SpreadQuoteRequest[T], A]]
    extends QuotingPolicy[T, SpreadQuoteRequest[T], SpreadQuote, A] {
  this: A =>

  def receive(request: SpreadQuoteRequest[T]): SpreadQuote = {
    SpreadQuote(request(orderBook))
  }

}