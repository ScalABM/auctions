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

import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingRule, BidQuotePricingRule, PricingRule}
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy


trait Auction[T <: Tradable] extends AuctionLike[T, Auction[T]]


object Auction {

  /** Create an `Auction` with a particular reservation price and pricing rule.
    *
    * @param reservation
    * @param rule
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitAskOrder[T], rule: PricingRule[T, Price]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), rule)
  }

  /** Create an `Auction` with a particular reservation price, pricing rule, and quoting policy.
    *
    * @param reservation
    * @param rule
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitAskOrder[T], rule: PricingRule[T, Price], policy: PriceQuotePolicy[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new OpenOrderBookImpl[T](orderBook.insert(reservation), rule, policy)
  }

  /** Create a first-price, sealed-bid `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def firstPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), new AskQuotePricingRule)
  }

  /** Create a second-price, sealed-bid or Vickrey `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def secondPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), new BidQuotePricingRule)
  }

  /** Create `WithClosedOrderBook` that encapsulates an order book containing a particular reservation price.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withClosedOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new WithClosedOrderBook[T](orderBook.insert(reservation))
  }

  /** Create an `WithOrderBook` that encapsulates an order book containing a particular reservation price.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withOpenOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithOpenOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T](LimitAskOrder.ordering, LimitBidOrder.ordering.reverse)
    new WithOpenOrderBook[T](orderBook.insert(reservation))
  }


  sealed abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitBidOrder[T]): WithOrderBook[T]

    def remove(order: LimitBidOrder[T]): WithOrderBook[T]

  }


  final class WithClosedOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook insert order)
    }

    def remove(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook remove order)
    }

    def withPricingRule(rule: PricingRule[T, Price]): Auction[T] = {
      new ClosedOrderBookImpl[T](orderBook, rule)
    }

  }


  final class WithOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook insert order)
    }

    def remove(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook remove order)
    }

    def withQuotePolicy(policy: PriceQuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook, policy)
    }

  }


  final class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: PriceQuotePolicy[T])
    extends WithOrderBook[T](orderBook) {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      policy(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook insert order, policy)
    }

    def remove(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook remove order, policy)
    }

    def withPricingRule(rule: PricingRule[T, Price]): Auction[T] = {
      new OpenOrderBookImpl[T](orderBook, rule, policy)
    }

  }


  private[this] class ClosedOrderBookImpl[T <: Tradable](_orderBook: FourHeapOrderBook[T], _pricingRule: PricingRule[T, Price])
    extends Auction[T] {

    def insert(order: LimitBidOrder[T]): Auction[T] = {
      new ClosedOrderBookImpl(orderBook insert order, pricingRule)
    }

    def remove(order: LimitBidOrder[T]): Auction[T] = {
      new ClosedOrderBookImpl(orderBook remove order, pricingRule)
    }

    def clear: (Option[Stream[Fill[T]]], Auction[T]) = {
      pricingRule(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new ClosedOrderBookImpl(newOrderBook, pricingRule))
        case None => (None, new ClosedOrderBookImpl(orderBook, pricingRule))
      }
    }

    protected val orderBook: FourHeapOrderBook[T] = _orderBook

    protected val pricingRule: PricingRule[T, Price] = _pricingRule

  }


  private[this] class OpenOrderBookImpl[T <: Tradable](_orderBook: FourHeapOrderBook[T], _pricingRule: PricingRule[T, Price], policy: PriceQuotePolicy[T])
    extends Auction[T] {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      policy(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): Auction[T] = {
      new OpenOrderBookImpl(orderBook insert order, pricingRule, policy)
    }

    def remove(order: LimitBidOrder[T]): Auction[T] = {
      new OpenOrderBookImpl(orderBook remove order, pricingRule, policy)
    }

    def clear: (Option[Stream[Fill[T]]], Auction[T]) = {
      pricingRule(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new OpenOrderBookImpl(newOrderBook, pricingRule, policy))
        case None => (None, new OpenOrderBookImpl(orderBook, pricingRule, policy))
      }
    }

    protected val orderBook: FourHeapOrderBook[T] = _orderBook

    protected val pricingRule: PricingRule[T, Price] = _pricingRule

  }

}