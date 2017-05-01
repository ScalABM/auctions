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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy}
import org.economicsl.auctions.singleunit.quoting.PriceQuotePolicy


trait Auction[T <: Tradable] extends AuctionLike[T, LimitBidOrder[T], Auction[T]]


object Auction {

  /** Create an `Auction` with a particular reservation price and pricing rule.
    *
    * @param reservation
    * @param policy
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitAskOrder[T], policy: PricingPolicy[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), policy)
  }

  /** Create an `Auction` with a particular reservation price, pricing rule, and quoting policy.
    *
    * @param reservation
    * @param pricing
    * @param quoting
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitAskOrder[T], pricing: PricingPolicy[T], quoting: PriceQuotePolicy[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenOrderBookImpl[T](orderBook.insert(reservation), pricing, quoting)
  }

  /** Create a first-price, sealed-bid `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def firstPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), new AskQuotePricingPolicy)
  }

  /** Create a second-price, sealed-bid or Vickrey `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def secondPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), new BidQuotePricingPolicy)
  }

  /** Create `WithClosedOrderBook` that encapsulates an order book containing a particular reservation price.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withClosedOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithClosedOrderBook[T](orderBook.insert(reservation))
  }

  /** Create an `WithOrderBook` that encapsulates an order book containing a particular reservation price.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withOpenOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithOpenOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOpenOrderBook[T](orderBook.insert(reservation))
  }


  sealed abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitBidOrder[T]): WithOrderBook[T]

    def remove(order: LimitBidOrder[T]): WithOrderBook[T]

  }


  final class WithClosedOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.remove(order))
    }

    def withPricingRule(policy: PricingPolicy[T]): Auction[T] = {
      new ClosedOrderBookImpl[T](orderBook, policy)
    }

  }


  final class WithOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook.remove(order))
    }

    def withQuotePolicy(policy: PriceQuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook, policy)
    }

  }


  final class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], quoting: PriceQuotePolicy[T])
    extends WithOrderBook[T](orderBook) {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quoting(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.insert(order), quoting)
    }

    def remove(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.remove(order), quoting)
    }

    def withPricingRule(policy: PricingPolicy[T]): Auction[T] = {
      new OpenOrderBookImpl[T](orderBook, policy, quoting)
    }

  }


  private[this] class ClosedOrderBookImpl[T <: Tradable](protected val orderBook: FourHeapOrderBook[T],
                                                         protected val pricing: PricingPolicy[T])
    extends Auction[T] {

    def insert(order: LimitBidOrder[T]): Auction[T] = {
      new ClosedOrderBookImpl(orderBook.insert(order), pricing)
    }

    def remove(order: LimitBidOrder[T]): Auction[T] = {
      new ClosedOrderBookImpl(orderBook.remove(order), pricing)
    }

    def clear: ClearResult[T, Auction[T]] = {
      pricing(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          ClearResult(Some(fills), new ClosedOrderBookImpl(newOrderBook, pricing))
        case None => ClearResult(None, this)
      }
    }

  }


  private[this] class OpenOrderBookImpl[T <: Tradable](protected val orderBook: FourHeapOrderBook[T],
                                                       protected val pricing: PricingPolicy[T],
                                                       protected val quoting: PriceQuotePolicy[T])
    extends Auction[T] {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quoting(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): Auction[T] = {
      new OpenOrderBookImpl(orderBook.insert(order), pricing, quoting)
    }

    def remove(order: LimitBidOrder[T]): Auction[T] = {
      new OpenOrderBookImpl(orderBook.remove(order), pricing, quoting)
    }

    def clear: ClearResult[T, Auction[T]] = {
      pricing(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          ClearResult(Some(fills), new OpenOrderBookImpl(newOrderBook, pricing, quoting))
        case None => ClearResult(None, new OpenOrderBookImpl(orderBook, pricing, quoting))
      }
    }

  }

}