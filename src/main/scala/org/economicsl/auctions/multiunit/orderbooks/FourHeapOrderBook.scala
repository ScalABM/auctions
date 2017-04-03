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
package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


class FourHeapOrderBook[T <: Tradable] private(matchedOrders: MatchedOrders[T], unMatchedOrders: UnMatchedOrders[T]) {

  def - (uuid: UUID): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(uuid)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - uuid)
    } else {
      val askOrder = matchedOrders.askOrders(uuid)
      val (uuid2, bidOrder) = matchedOrders.bidOrders.head
      val excessDemand = bidOrder.quantity - askOrder.quantity
      if (excessDemand > Quantity(0)) {
        val (matched, residual) = split(bidOrder, excessDemand)
        new FourHeapOrderBook(matchedOrders.removeAndReplace((uuid, askOrder), (uuid2, residual)), unMatchedOrders.updated(uuid2, matched))
      } else if (excessDemand < Quantity(0)) {
        ??? // split the ask order; removed the matched portion of the askOrder and the bidOrder; recurse with the residual askOrder; add residual askOrder to unMatchedOrders
      } else {
        ??? // remove the askOrder and the bidOrder from the matched orders
      }
    }
  }

  def takeWhileMatched: (Stream[(LimitAskOrder[T], LimitBidOrder[T])], FourHeapOrderBook[T]) = {
    (matchedOrders.zipped, withEmptyMatchedOrders)
  }

  def updated(uuid: UUID, order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some((_, askOrder)), Some((uuid2, bidOrder))) if order.value <= bidOrder.value && askOrder.value <= bidOrder.value =>
        val excessDemand = bidOrder.quantity - order.quantity
        if (excessDemand > Quantity(0)) {
          val residualUnMatchedOrders = unMatchedOrders - uuid2
          val (filled, residual) = (bidOrder.withQuantity(order.quantity), bidOrder.withQuantity(excessDemand))  // split the bidOrder!
          new FourHeapOrderBook(matchedOrders.updated((uuid, order), (uuid2, filled)), residualUnMatchedOrders.updated(uuid2, residual))
        } else if (excessDemand < Quantity(0)) {
          ???
        } else {
          new FourHeapOrderBook(matchedOrders.updated((uuid, order), (uuid2, bidOrder)), unMatchedOrders - uuid2)
        }

      case (Some(askOrder), _) if order.value <= askOrder.value =>
        ???
      case _ => new FourHeapOrderBook(matchedOrders, unMatchedOrders.updated(uuid, order))
    }
  }

  def updated(uuid: UUID, order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some((???, askOrder)), Some((_, bidOrder))) if order.value <= bidOrder.value && askOrder.value <= bidOrder.value =>
        ???
      case (Some(askOrder), _) if order.value <= askOrder.value =>
        ???
      case _ => new FourHeapOrderBook(matchedOrders, unMatchedOrders.updated(uuid, order))
    }
  }

  private[this] def withEmptyMatchedOrders: FourHeapOrderBook[T] = {
    val (askOrdering, bidOrdering) = (matchedOrders.askOrdering, matchedOrders.bidOrdering)
    new FourHeapOrderBook[T](MatchedOrders.empty(askOrdering, bidOrdering), unMatchedOrders)
  }

  private[this] def split(order: LimitAskOrder[T], residual: Quantity): (LimitAskOrder[T], LimitAskOrder[T]) = {
    val matched = order.quantity - residual  // todo consider checking that order.quantity is greater than residual!
    (order.withQuantity(matched), order.withQuantity(residual))
  }

  private[this] def split(order: LimitBidOrder[T], residual: Quantity): (LimitBidOrder[T], LimitBidOrder[T]) = {
    val matched = order.quantity - residual  // todo consider checking that order.quantity is greater than residual!
    (order.withQuantity(matched), order.withQuantity(residual))
  }

}


object FourHeapOrderBook {

  def empty[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty(askOrdering.reverse, bidOrdering.reverse)
    val unMatchedOrders = UnMatchedOrders.empty(askOrdering, bidOrdering)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

