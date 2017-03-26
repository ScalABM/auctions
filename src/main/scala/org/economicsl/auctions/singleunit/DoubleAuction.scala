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
import org.economicsl.auctions.singleunit.pricing.PricingRule


/** Base trait for all double auction implementations. */
trait DoubleAuction[T <: Tradable] extends AuctionLike[T, DoubleAuction[T]] with ReverseAuctionLike[T, DoubleAuction[T]] {

  def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], DoubleAuction[T])

}


object DoubleAuction {

  def withUniformPricing[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): DoubleAuction[T] = {
    new UniformPriceImpl[T](FourHeapOrderBook.empty[T](askOrdering, bidOrdering))
  }

  def withDiscriminatoryPricing[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): DoubleAuction[T] = {
    new DiscriminatoryPriceImpl[T](FourHeapOrderBook.empty[T](askOrdering, bidOrdering))
  }

  private[this] class UniformPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook + order)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new UniformPriceImpl(orderBook - order)
    }

    def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
      p(orderBook) match {
        case Some(price) =>
          val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
          val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
          (Some(fills), new UniformPriceImpl(newOrderBook))
        case None => (None, new UniformPriceImpl(orderBook))
      }
    }

  }


  private[this] class DiscriminatoryPriceImpl[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends DoubleAuction[T] {

    def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order)
    }

    def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook + order)
    }

    def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order)
    }

    def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
      new DiscriminatoryPriceImpl(orderBook - order)
    }

    def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {

      @annotation.tailrec
      def loop(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
        p(ob) match {
          case None => (if (fills.nonEmpty) Some(fills) else None, new DiscriminatoryPriceImpl(ob))
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
