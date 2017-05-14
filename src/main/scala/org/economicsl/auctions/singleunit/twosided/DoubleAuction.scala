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
package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.singleunit.{AskOrder, AuctionLike, BidOrder, Fill, Order}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.quoting.{PriceQuotePolicy, PriceQuoting}


/** Base trait for all double auction implementations. */
trait DoubleAuction[T <: Tradable] extends AuctionLike[T, Order[T], DoubleAuction[T]]


object DoubleAuction {

  def withDiscriminatoryPricing[T <: Tradable](policy: PricingPolicy[T]): DoubleAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new DiscriminatoryPriceImpl[T](orderBook, policy)
  }

  def withDiscriminatoryPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: PricingPolicy[T]): DoubleAuction[T] = {
    new DiscriminatoryPriceImpl[T](orderBook, policy)
  }

  def withDiscriminatoryPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], pricing: PricingPolicy[T], quoting: PriceQuotePolicy[T]): DoubleAuction[T] with PriceQuoting = {
    new DiscriminatoryPriceImpl2[T](orderBook, pricing, quoting)
  }

  def withClosedOrderBook[T <: Tradable]: WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithClosedOrderBook[T](orderBook)
  }

  def withClosedOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]): WithClosedOrderBook[T] = {
    new WithClosedOrderBook[T](orderBook)
  }

  def withOpenOrderBook[T <: Tradable]: WithOpenOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOpenOrderBook[T](orderBook)
  }

  def withOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]): WithOpenOrderBook[T] = {
    new WithOpenOrderBook[T](orderBook)
  }

  def withUniformPricing[T <: Tradable](policy: PricingPolicy[T]): DoubleAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new UniformPriceImpl[T](orderBook, policy)
  }

  def withUniformPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: PricingPolicy[T]): DoubleAuction[T] = {
    new UniformPriceImpl[T](orderBook, policy)
  }

  def withUniformPricing[T <: Tradable](orderBook: FourHeapOrderBook[T], pricing: PricingPolicy[T], quoting: PriceQuotePolicy[T]): DoubleAuction[T] with PriceQuoting = {
    new UniformPriceImpl2[T](orderBook, pricing, quoting)
  }

  sealed abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

    def insert(order: Order[T]): WithOrderBook[T]

    def remove(order: Order[T]): WithOrderBook[T]

  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  final class WithClosedOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: Order[T]): WithClosedOrderBook[T] = order match {
      case askOrder: AskOrder[T] => new WithClosedOrderBook(orderBook.insert(askOrder))
      case bidOrder: BidOrder[T] => new WithClosedOrderBook(orderBook.insert(bidOrder))
    }

    def remove(order: Order[T]): WithClosedOrderBook[T] = order match {
      case askOrder: AskOrder[T] => new WithClosedOrderBook(orderBook.remove(askOrder))
      case bidOrder: BidOrder[T] => new WithClosedOrderBook(orderBook.remove(bidOrder))
    }

    def withDiscriminatoryPricing(policy: PricingPolicy[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl[T](orderBook, policy)
    }

    def withUniformPricing(policy: PricingPolicy[T]): DoubleAuction[T] = {
      new UniformPriceImpl[T](orderBook, policy)
    }

  }

  /** Class that allows the user to create a `DoubleAuction` with a particular `orderBook` but leaving the pricing rule undefined. */
  final class WithOpenOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: Order[T]): WithOpenOrderBook[T] = order match {
      case askOrder: AskOrder[T] => new WithOpenOrderBook(orderBook.insert(askOrder))
      case bidOrder: BidOrder[T] => new WithOpenOrderBook(orderBook.insert(bidOrder))
    }

    def remove(order: Order[T]): WithOpenOrderBook[T] = order match {
      case askOrder: AskOrder[T] => new WithOpenOrderBook(orderBook.remove(askOrder))
      case bidOrder: BidOrder[T] => new WithOpenOrderBook(orderBook.remove(bidOrder))
    }

    def withQuotePolicy(policy: PriceQuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy(orderBook, policy)
    }

  }

  final class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], policy: PriceQuotePolicy[T])
    extends WithOrderBook[T](orderBook) with PriceQuoting {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      policy(orderBook, request)
    }

    def insert(order: Order[T]): WithQuotePolicy[T] = order match {
      case askOrder: AskOrder[T] => new WithQuotePolicy(orderBook.insert(askOrder), policy)
      case bidOrder: BidOrder[T] => new WithQuotePolicy(orderBook.insert(bidOrder), policy)
    }

    def remove(order: Order[T]): WithQuotePolicy[T] = order match {
      case askOrder: AskOrder[T] => new WithQuotePolicy(orderBook.remove(askOrder), policy)
      case bidOrder: BidOrder[T] => new WithQuotePolicy(orderBook.remove(bidOrder), policy)
    }

    def withDiscriminatoryPricing(pricingRule: PricingPolicy[T]): DoubleAuction[T] with PriceQuoting = {
      new DiscriminatoryPriceImpl2[T](orderBook, pricingRule, policy)
    }

    def withUniformPricing(pricingRule: PricingPolicy[T]): DoubleAuction[T] with PriceQuoting = {
      new UniformPriceImpl2[T](orderBook, pricingRule, policy)
    }

  }


  private[this] class UniformPriceImpl[T <: Tradable] (protected val orderBook: FourHeapOrderBook[T],
                                                       protected val pricingPolicy: PricingPolicy[T])
    extends DoubleAuction[T] {

    def insert(order: Order[T]): DoubleAuction[T] = order match {
      case offer: AskOrder[T] => new UniformPriceImpl(orderBook.insert(offer), pricingPolicy)
      case bid: BidOrder[T] => new UniformPriceImpl(orderBook.insert(bid), pricingPolicy)
    }

    def remove(order: Order[T]): DoubleAuction[T] = order match {
      case offer: AskOrder[T] => new UniformPriceImpl(orderBook.remove(offer), pricingPolicy)
      case bid: BidOrder[T] => new UniformPriceImpl(orderBook.remove(bid), pricingPolicy)
    }

    def clear: ClearResult[T, DoubleAuction[T]]  = {
      pricingPolicy(orderBook) match {
        case Some(price) =>
          val (pairedOrders, residual) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          ClearResult[T, DoubleAuction[T]](Some(fills), new UniformPriceImpl(residual, pricingPolicy))
        case None => ClearResult[T, DoubleAuction[T]](None, this)
      }
    }

  }


  private[this] class UniformPriceImpl2[T <: Tradable] (protected val orderBook: FourHeapOrderBook[T],
                                                        protected val pricingPolicy: PricingPolicy[T],
                                                        protected val quoting: PriceQuotePolicy[T])
    extends DoubleAuction[T] with PriceQuoting {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quoting(orderBook, request)
    }

    def insert(order: Order[T]): DoubleAuction[T] with PriceQuoting = order match {
      case offer: AskOrder[T] => new UniformPriceImpl2(orderBook.insert(offer), pricingPolicy, quoting)
      case bid: BidOrder[T] => new UniformPriceImpl2(orderBook.insert(bid), pricingPolicy, quoting)
    }

    def remove(order: Order[T]): DoubleAuction[T] with PriceQuoting = order match {
      case offer: AskOrder[T] => new UniformPriceImpl2(orderBook.remove(offer), pricingPolicy, quoting)
      case bid: BidOrder[T] => new UniformPriceImpl2(orderBook.remove(bid), pricingPolicy, quoting)
    }

    def clear: ClearResult[T, DoubleAuction[T] with PriceQuoting] = {
      pricingPolicy(orderBook) match {
        case Some(price) =>
          val (pairedOrders, residual) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          ClearResult[T, DoubleAuction[T] with PriceQuoting](Some(fills), new UniformPriceImpl2(residual, pricingPolicy, quoting))
        case None => ClearResult[T, DoubleAuction[T] with PriceQuoting](None, this)
      }
    }

  }


  private[this] class DiscriminatoryPriceImpl[T <: Tradable] (protected val orderBook: FourHeapOrderBook[T],
                                                              protected val pricingPolicy: PricingPolicy[T])
    extends DoubleAuction[T] {

    def insert(order: Order[T]): DoubleAuction[T] = order match {
      case offer: AskOrder[T] => new DiscriminatoryPriceImpl(orderBook.insert(offer), pricingPolicy)
      case bid: BidOrder[T] => new DiscriminatoryPriceImpl(orderBook.insert(bid), pricingPolicy)
    }

    def remove(order: Order[T]): DoubleAuction[T] = order match {
      case offer: AskOrder[T] => new DiscriminatoryPriceImpl(orderBook.remove(offer), pricingPolicy)
      case bid: BidOrder[T] => new DiscriminatoryPriceImpl(orderBook.remove(bid), pricingPolicy)
    }

    def clear: ClearResult[T, DoubleAuction[T]] = {

      @annotation.tailrec
      def loop(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): ClearResult[T, DoubleAuction[T]] = {
        val currentPrice = pricingPolicy(ob)
        val (bestMatch, residual) = ob.takeBestMatched
        bestMatch match {
          case Some((askOrder, bidOrder)) =>
            val fill = currentPrice.map(price => Fill(askOrder, bidOrder, price))
            loop(fill.fold(fills)(_ #:: fills), residual)
          case None =>
            val results = if (fills.nonEmpty) Some(fills) else None
            ClearResult[T, DoubleAuction[T]](results, new DiscriminatoryPriceImpl(residual, pricingPolicy))
        }
      }
      loop(Stream.empty, orderBook)

    }

  }


  private[this] class DiscriminatoryPriceImpl2[T <: Tradable] (protected val orderBook: FourHeapOrderBook[T],
                                                               protected val pricingPolicy: PricingPolicy[T],
                                                               protected val quoting: PriceQuotePolicy[T])
    extends DoubleAuction[T] with PriceQuoting {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quoting(orderBook, request)
    }

    def insert(order: Order[T]): DoubleAuction[T] with PriceQuoting = order match {
      case offer: AskOrder[T] => new DiscriminatoryPriceImpl2(orderBook.insert(offer), pricingPolicy, quoting)
      case bid: BidOrder[T] => new DiscriminatoryPriceImpl2(orderBook.insert(bid), pricingPolicy, quoting)
    }

    def remove(order: Order[T]): DoubleAuction[T] with PriceQuoting = order match {
      case offer: AskOrder[T] => new DiscriminatoryPriceImpl2(orderBook.remove(offer), pricingPolicy, quoting)
      case bid: BidOrder[T] => new DiscriminatoryPriceImpl2(orderBook.remove(bid), pricingPolicy, quoting)
    }

    def clear: ClearResult[T, DoubleAuction[T] with PriceQuoting] = {

      @annotation.tailrec
      def loop(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): ClearResult[T, DoubleAuction[T] with PriceQuoting] = {
        val (bestMatch, residual) = ob.takeBestMatched
        bestMatch match {
          case Some((askOrder, bidOrder)) =>
            val currentPrice = pricingPolicy(ob)
            val fill = currentPrice.map(price => Fill(askOrder, bidOrder, price))
            loop(fill.fold(fills)(_ #:: fills), residual)
          case None =>
            val results = if (fills.nonEmpty) Some(fills) else None
            ClearResult[T, DoubleAuction[T] with PriceQuoting](results, new DiscriminatoryPriceImpl2(residual, pricingPolicy, quoting))
        }
      }
      loop(Stream.empty, orderBook)

    }

  }

}
