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
import org.economicsl.auctions.singleunit.quotes.QuotePolicy


/** Base trait for all double auction implementations. */
trait DoubleAuction[T <: Tradable] extends AuctionLike[T, DoubleAuction[T]] with ReverseAuctionLike[T, DoubleAuction[T]]


object DoubleAuction {

  def withDiscriminatoryPricing[T <: Tradable](rule: PricingRule[T, Price]): DoubleAuction[T] = {
    new DiscriminatoryPriceImpl[T](FourHeapOrderBook.empty[T], rule)
  }

  def withDiscriminatoryPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price]): DoubleAuction[T] = {
    new DiscriminatoryPriceImpl[T](orderBook, rule)
  }

  def withDiscriminatoryPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price], policy: QuotePolicy[T]): DoubleAuction[T] = {
    ???
  }

  def withClosedOrderBook[T <: Tradable]: WithClosedOrderBook[T] = {
    new WithClosedOrderBook[T](FourHeapOrderBook.empty[T])
  }

  def withClosedOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]): WithClosedOrderBook[T] = {
    new WithClosedOrderBook[T](orderBook)
  }

  def withOpenOrderBook[T <: Tradable]: WithOpenOrderBook[T] = {
    new WithOpenOrderBook[T](FourHeapOrderBook.empty[T])
  }

  def withOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]): WithOpenOrderBook[T] = {
    new WithOpenOrderBook[T](orderBook)
  }

  def withUniformPricing[T <: Tradable](rule: PricingRule[T, Price]): DoubleAuction[T] = {
    new UniformPriceImpl[T](FourHeapOrderBook.empty[T], rule)
  }

  def withUniformPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price]): DoubleAuction[T] = {
    new UniformPriceImpl[T](orderBook,rule)
  }

  def withUniformPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price], policy: QuotePolicy[T]): DoubleAuction[T] = {
    ???
  }

  def withClosedOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithClosedOrderBook[T](orderBook + reservation)
  }

  def withOpenOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithOpenOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOpenOrderBook[T](orderBook + reservation)
  }


  sealed abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

    def insert(order: LimitAskOrder[T]): WithOrderBook[T]

    def insert(order: LimitBidOrder[T]): WithOrderBook[T]

    def remove(order: LimitAskOrder[T]): WithOrderBook[T]

    def remove(order: LimitBidOrder[T]): WithOrderBook[T]

  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  final class WithClosedOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitAskOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook(orderBook + order)
    }

    def insert(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook(orderBook + order)
    }

    def remove(order: LimitAskOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook(orderBook - order)
    }

    def remove(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook(orderBook - order)
    }

    def withDiscriminatoryPricing(rule: PricingRule[T, Price]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl[T](orderBook, rule)
    }

    def withQuotePolicy(policy: QuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook, policy)
    }

    def withUniformPricing(rule: PricingRule[T, Price]): DoubleAuction[T] = {
      new UniformPriceImpl[T](orderBook, rule)
    }

  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  final class WithOpenOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitAskOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook(orderBook + order)
    }

    def insert(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook(orderBook + order)
    }

    def remove(order: LimitAskOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook(orderBook - order)
    }

    def remove(order: LimitBidOrder[T]):WithOpenOrderBook[T] = {
      new WithOpenOrderBook(orderBook - order)
    }

    def withQuotePolicy(policy: QuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook, policy)
    }

  }

  final class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: QuotePolicy[T])
    extends WithOrderBook[T](orderBook) {

    def receive(request: QuoteRequest): Option[Quote] = {
      policy(orderBook, request)
    }

    def insert(order: LimitAskOrder[T]): WithQuotePolicy[T] = {
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
      ???
    }

    def withUniformPricing(pricingRule: PricingRule[T, Price]): DoubleAuction[T] = {
      ???
    }

  }


  private[this] class UniformPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price])
    extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order, rule)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order, rule)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order, rule)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order, rule)
    }

    def clear: (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
      rule(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new UniformPriceImpl(newOrderBook, rule))
        case None => (None, new UniformPriceImpl(orderBook, rule))
      }
    }

  }


  private[this] class DiscriminatoryPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T], rule: PricingRule[T, Price])
    extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order, rule)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order, rule)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order, rule)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order, rule)
    }

    def clear: (Option[Stream[Fill[T]]], DoubleAuction[T]) = {

      @annotation.tailrec
      def loop(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
        rule(ob) match {
          case None => (if (fills.nonEmpty) Some(fills) else None, new DiscriminatoryPriceImpl(ob, rule))
          case Some(price) =>
            val (bestMatch, residual) = ob.takeBestMatched
            val fill = bestMatch.map{ case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
            loop(fill.fold(fills)(head => head #:: fills), residual)
        }
      }
      loop(Stream.empty, orderBook)

    }

  }

}
