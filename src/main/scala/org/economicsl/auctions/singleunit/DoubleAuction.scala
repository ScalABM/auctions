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
import org.economicsl.auctions.singleunit.pricing.PricingRule
import org.economicsl.auctions.singleunit.quotes.{ClosedOrderBookPolicy, QuotePolicy}


/** Base trait for all double auction implementations. */
trait DoubleAuction[T <: Tradable] extends AuctionLike[T, DoubleAuction[T]] with ReverseAuctionLike[T, DoubleAuction[T]]


object DoubleAuction {

  /** Create "sealed-bid/ask" double auction with discriminatory pricing. */
  def withDiscriminatoryPricing[T <: Tradable](pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
    new DiscriminatoryPriceImpl[T](FourHeapOrderBook.empty[T], pricingRule, new ClosedOrderBookPolicy)
  }

  def withOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]): WithOrderBook[T] = {
    new WithOrderBook(orderBook)
  }

  def withQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: QuotePolicy[T]): WithQuotePolicy[T] = {
    new WithQuotePolicy(orderBook, policy)
  }

  /** Create "sealed-bid/ask" double auction with uniform pricing. */
  def withUniformPricing[T <: Tradable](pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
    new UniformPriceImpl[T](FourHeapOrderBook.empty[T], pricingRule, new ClosedOrderBookPolicy)
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

    def withDiscriminatoryPricing(pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl[T](orderBook, pricingRule, new ClosedOrderBookPolicy)
    }

    def withQuotePolicy(policy: QuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook, policy)
    }

    def withUniformPricing(pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
      new UniformPriceImpl[T](orderBook, pricingRule, new ClosedOrderBookPolicy)
    }

  }

  class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: QuotePolicy[T]) {

    def receive(request: QuoteRequest): Option[Quote] = {
      policy(orderBook, request)
    }

    def insert(order: LimitAskOrder[T]):WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook + order, policy)
    }

    def insert(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook + order, policy)
    }

    def remove(order: LimitAskOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook - order, policy)
    }

    def remove(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook - order, policy)
    }

    def withDiscriminatoryPricing(pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl[T](orderBook, pricingRule, new ClosedOrderBookPolicy)
    }

    def withUniformPricing(pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
      new UniformPriceImpl[T](orderBook, pricingRule, new ClosedOrderBookPolicy)
    }

  }


  private[this] class UniformPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T], pricingRule: PricingRule[T, Price], policy: QuotePolicy[T])
    extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order, pricingRule, policy)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order, pricingRule, policy)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order, pricingRule, policy)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order, pricingRule, policy)
    }

    def clear: (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
      pricingRule(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new UniformPriceImpl(newOrderBook, pricingRule, policy))
        case None => (None, new UniformPriceImpl(orderBook, pricingRule, policy))
      }
    }

  }


  private[this] class DiscriminatoryPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T], pricingRule: PricingRule[T, Price], policy: QuotePolicy[T])
    extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order, pricingRule, policy)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order, pricingRule, policy)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order, pricingRule, policy)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order, pricingRule, policy)
    }

    def clear: (Option[Stream[Fill[T]]], DoubleAuction[T]) = {

      @annotation.tailrec
      def loop(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
        p(ob) match {
          case None => (if (fills.nonEmpty) Some(fills) else None, new DiscriminatoryPriceImpl(ob, pricingRule, policy))
          case Some(price) =>
            val (bestMatch, residual) = ob.takeBestMatched
            val fill = bestMatch.map{ case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
            loop(fill.fold(fills)(head => head #:: fills), residual)
        }
      }
      loop(Stream.empty, orderBook)

    }

    protected val p: PricingRule[T, Price] = pricingRule

  }

}
