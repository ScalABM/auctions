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

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


class FourHeapOrderBook[T <: Tradable] private(matchedOrders: MatchedOrders[T], unMatchedOrders: UnMatchedOrders[T]) {

  def - (uuid: UUID): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(uuid)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - uuid)
    } else {
      ???
    }
  }

  def + (kv: (UUID, LimitAskOrder[T])): FourHeapOrderBook[T] = {
    updated(kv._1, kv._2)
  }

  def + (kv: (UUID, LimitBidOrder[T])): FourHeapOrderBook[T] = {
    updated(kv._1, kv._2)
  }

  def takeWhileMatched: (Stream[(LimitAskOrder[T], LimitBidOrder[T])], FourHeapOrderBook[T]) = {
    (matchedOrders.zipped, withEmptyMatchedOrders)
  }

  def updated(uuid: UUID, order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) if order.value <= bidOrder.value && askOrder.value <= bidOrder.value =>
        ???
      case (Some(askOrder), _) if order.value <= askOrder.value =>
        ???
      case _ => new FourHeapOrderBook(matchedOrders, unMatchedOrders.updated(uuid, order))
    }
  }

  def updated(uuid: UUID, order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) if order.value <= bidOrder.value && askOrder.value <= bidOrder.value =>
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

}


object FourHeapOrderBook {

  def empty[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty(askOrdering.reverse, bidOrdering.reverse)
    val unMatchedOrders = UnMatchedOrders.empty(askOrdering, bidOrdering)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

