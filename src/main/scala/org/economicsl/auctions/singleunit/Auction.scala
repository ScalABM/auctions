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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.quotes.{Quote, QuoteRequest}
import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingRule, BidQuotePricingRule, PricingRule}
import org.economicsl.auctions.singleunit.quotes.{ClosedOrderBookPolicy, QuotePolicy}


class Auction[T <: Tradable] private(orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price], policy: QuotePolicy[T])
  extends AuctionLike[T, Auction[T]] {

  def insert(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook + order, rule, policy)
  }

  def remove(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook - order, rule, policy)
  }

  def clear: (Option[Stream[Fill[T]]], Auction[T]) = {
    rule(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new Auction(newOrderBook, rule, policy))
      case None => (None, new Auction(orderBook, rule, policy))
    }
  }

}


object Auction{

  def apply[T <: Tradable](reservation: LimitAskOrder[T], rule: PricingRule[T, Price], policy: QuotePolicy[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, rule, policy)
  }

  def firstPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, new AskQuotePricingRule, new ClosedOrderBookPolicy)
  }

  def secondPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, new BidQuotePricingRule, new ClosedOrderBookPolicy)
  }

  def withReservationPrice[T <: Tradable](reservation: LimitAskOrder[T]): WithOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOrderBook(orderBook + reservation)
  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  class WithOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitBidOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook + order)
    }

    def remove(order: LimitBidOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook - order)
    }

    def withClosedOrderBook(pricingRule: PricingRule[T, Price]): Auction[T] = {
      new Auction(orderBook, pricingRule, new ClosedOrderBookPolicy)
    }

    def withQuotingPolicy(policy: QuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook, policy)
    }

  }

  class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: QuotePolicy[T]) {

    def receive(request: QuoteRequest): Option[Quote] = {
      policy(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook + order, policy)
    }

    def remove(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook - order, policy)
    }

    def withPricingRule(rule: PricingRule[T, Price]): Auction[T] = {
      new Auction(orderBook, rule, new ClosedOrderBookPolicy)
    }

  }

}