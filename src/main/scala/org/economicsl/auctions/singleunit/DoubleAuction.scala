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


class DoubleAuction[T <: Tradable] private(orderBook: FourHeapOrderBook[T]) {

  def insert(order: LimitAskOrder[T]): DoubleAuction[T] = {
    new DoubleAuction(orderBook + order)
  }

  def insert(order: LimitBidOrder[T]): DoubleAuction[T] = {
    new DoubleAuction(orderBook + order)
  }

  def remove(order: LimitAskOrder[T]): DoubleAuction[T] = {
    new DoubleAuction(orderBook - order)
  }

  def remove(order: LimitBidOrder[T]): DoubleAuction[T] = {
    new DoubleAuction(orderBook - order)
  }

  def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
    p(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeWhileMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new DoubleAuction(newOrderBook))
      case None => (None, new DoubleAuction(orderBook))
    }
  }

}


object DoubleAuction {

  def withEmptyOrderBook[T <: Tradable]: DoubleAuction[T] = {
    new DoubleAuction(FourHeapOrderBook.empty[T])
  }

}
