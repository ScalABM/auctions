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
import org.economicsl.auctions.singleunit.pricing.{BidQuotePricingPolicy, PricingPolicy}
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy


trait ReverseAuction[T <: Tradable] extends ReverseAuctionLike[T, ReverseAuction[T]]


object ReverseAuction {

  /** Create a `ReverseAuction` with a particular reservation price and pricing policy.
    *
    * @param reservation
    * @param policy
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitBidOrder[T], policy: PricingPolicy[T]): ReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), policy)
  }

  /** Create a `ReverseAuction` with a particular reservation price, pricing policy, and quoting policy.
    *
    * @param reservation
    * @param pricing
    * @param quoting
    * @tparam T
    * @return
    */
  def apply[T <: Tradable](reservation: LimitBidOrder[T], pricing: PricingPolicy[T], quoting: PriceQuotePolicy[T]): ReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenOrderBookImpl[T](orderBook.insert(reservation), pricing, quoting)
  }

  /** Create a first-price, sealed-aks reverse auction (FPSARA).
    *
    * @param reservation
    * @tparam T
    * @return
    * @note The winner of a FPSARA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to its own ask price.
    */
  def firstPriceSealedAsk[T <: Tradable](reservation: LimitBidOrder[T]): ReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), new BidQuotePricingPolicy)
  }

  /** Create a second-price, sealed-ask reverse auction (SPSARA).
    *
    * @param reservation
    * @tparam T
    * @return
    * @note The winner of a SPSARA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to the minimum of the second lowest submitted ask price and the buyer's `reservation` price.
    */
  def secondPriceSealedAsk[T <: Tradable](reservation: LimitBidOrder[T]): ReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    val policy = new PricingPolicy[T] { // todo can this pricing policy be generalized?
      def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
        orderBook.matchedOrders.bidOrders.headOption.flatMap {
          bidOrder => orderBook.unMatchedOrders.askOrders.headOption.map(askOrder => bidOrder.limit min askOrder.limit)
        }
      }
    }
    new ClosedOrderBookImpl[T](orderBook.insert(reservation), policy)
  }

  /** Create `WithClosedOrderBook` that encapsulates an order book containing a particular reservation price.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withClosedOrderBook[T <: Tradable](reservation: LimitBidOrder[T]): WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithClosedOrderBook[T](orderBook.insert(reservation))
  }

  sealed abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitAskOrder[T]): WithOrderBook[T]

    def remove(order: LimitAskOrder[T]): WithOrderBook[T]

  }


  final class WithClosedOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitAskOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitAskOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.remove(order))
    }

    def withPricingPolicy(policy: PricingPolicy[T]): ReverseAuction[T] = {
      new ClosedOrderBookImpl[T](orderBook, policy)
    }

  }


  final class WithOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitAskOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitAskOrder[T]): WithOpenOrderBook[T] = {
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

    def insert(order: LimitAskOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.insert(order), quoting)
    }

    def remove(order: LimitAskOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.remove(order), quoting)
    }

    def withPricingPolicy(policy: PricingPolicy[T]): ReverseAuction[T] = {
      new OpenOrderBookImpl[T](orderBook, policy, quoting)
    }

  }

  private[this] class ClosedOrderBookImpl[T <: Tradable](protected val orderBook: FourHeapOrderBook[T],
                                                         protected val pricing: PricingPolicy[T])
    extends ReverseAuction[T] {

    def insert(order: LimitAskOrder[T]): ReverseAuction[T] = {
      new ClosedOrderBookImpl(orderBook.insert(order), pricing)
    }

    def remove(order: LimitAskOrder[T]): ReverseAuction[T] = {
      new ClosedOrderBookImpl(orderBook.remove(order), pricing)
    }

    def clear: (Option[Stream[Fill[T]]], ReverseAuction[T]) = {
      pricing(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new ClosedOrderBookImpl(newOrderBook, pricing))
        case None => (None, new ClosedOrderBookImpl(orderBook, pricing))
      }
    }

  }


  private[this] class OpenOrderBookImpl[T <: Tradable](protected val orderBook: FourHeapOrderBook[T],
                                                       protected val pricing: PricingPolicy[T],
                                                       protected val quoting: PriceQuotePolicy[T])
    extends ReverseAuction[T] {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quoting(orderBook, request)
    }

    def insert(order: LimitAskOrder[T]): ReverseAuction[T] = {
      new OpenOrderBookImpl(orderBook.insert(order), pricing, quoting)
    }

    def remove(order: LimitAskOrder[T]): ReverseAuction[T] = {
      new OpenOrderBookImpl(orderBook.remove(order), pricing, quoting)
    }

    def clear: (Option[Stream[Fill[T]]], ReverseAuction[T]) = {
      pricing(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new OpenOrderBookImpl(newOrderBook, pricing, quoting))
        case None => (None, new OpenOrderBookImpl(orderBook, pricing, quoting))
      }
    }

  }

}
