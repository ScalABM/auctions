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

import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingRule, BidQuotePricingRule, PricingRule}


class Auction[T <: Tradable] private(orderBook: FourHeapOrderBook[T], pricingRule: PricingRule[T, Price])
  extends AuctionLike[T, Auction[T]] {

  def insert(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook + order, pricingRule)
  }

  def remove(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook - order, pricingRule)
  }

  def clear: (Option[Stream[Fill[T]]], Auction[T]) = {
    p(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new Auction(newOrderBook, pricingRule))
      case None => (None, new Auction(orderBook, pricingRule))
    }
  }

  protected val p: PricingRule[T, Price] = pricingRule

}


object Auction{

  def apply[T <: Tradable](reservation: LimitAskOrder[T], pricingRule: PricingRule[T, Price]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, pricingRule)
  }

  def firstPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, new AskQuotePricingRule)
  }

  def secondPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new Auction(orderBook + reservation, new BidQuotePricingRule)
  }

  def withReservationPrice[T <: Tradable](reservation: LimitAskOrder[T]): WithOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOrderBook(orderBook + reservation)
  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  class WithOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitAskOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook + order)
    }

    def insert(order: LimitBidOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook + order)
    }

    def remove(order: LimitAskOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook - order)
    }

    def remove(order: LimitBidOrder[T]): WithOrderBook[T] = {
      new WithOrderBook(orderBook - order)
    }

    def witPricing(pricingRule: PricingRule[T, Price]): Auction[T] = {
      new Auction(orderBook, pricingRule)
    }

  }

}