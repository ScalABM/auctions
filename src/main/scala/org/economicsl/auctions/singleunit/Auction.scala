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


class Auction[T <: Tradable] private(orderBook: FourHeapOrderBook[T]) extends AuctionLike[T, Auction[T]] {

  def insert(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook + order)
  }

  def remove(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook - order)
  }

  def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], Auction[T]) = {
    p(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new Auction(newOrderBook))
      case None => (None, new Auction(orderBook))
    }
  }

}


object Auction{

  def apply[T <: Tradable](initial: LimitAskOrder[T])(implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): Auction[T] = {
    val orderBook = FourHeapOrderBook.empty[T](askOrdering, bidOrdering)
    new Auction[T](orderBook + initial)
  }

}