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
package org.economicsl.auctions.singleunit.orderbooks

import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.{LimitAskOrder, LimitBidOrder}


class FourHeapOrderBook[T <: Tradable] private(matchedOrders: MatchedOrders[T], unMatchedOrders: UnMatchedOrders[T]) {

  // value of lowest matched bid must exceed value of highest unmatched bid!
  require(matchedOrders.bidOrders.headOption.forall(b1 => unMatchedOrders.bidOrders.headOption.forall(b2 => b1.value >= b2.value)))
  // value of lowest unmatched ask must exceed value of highest matched ask!
  require(unMatchedOrders.askOrders.headOption.forall(a1 => matchedOrders.askOrders.headOption.forall(a2 => a1.value >= a2.value)))

  def - (order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else {
      val bidOrder = matchedOrders.bidOrders.head
      new FourHeapOrderBook(matchedOrders - (order, bidOrder), unMatchedOrders + bidOrder)
    }
  }

  def - (order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else {
      val askOrder = matchedOrders.askOrders.head
      new FourHeapOrderBook(matchedOrders - (askOrder, order), unMatchedOrders + askOrder)
    }
  }

  def + (order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) if order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (order, bidOrder), unMatchedOrders - bidOrder)
      case (None, Some(bidOrder)) if order.limit <= bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (order, bidOrder), unMatchedOrders - bidOrder)
      case (Some(askOrder), Some(_)) if order.limit < askOrder.limit =>
        new FourHeapOrderBook(matchedOrders.replace(askOrder, order), unMatchedOrders + askOrder)
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  def + (order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
      case (Some(bidOrder), Some(askOrder)) if order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (askOrder, order), unMatchedOrders - askOrder)
      case (None, Some(askOrder)) if order.limit >= askOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (askOrder, order), unMatchedOrders - askOrder)
      case (Some(bidOrder), Some(_)) if order.limit > bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders.replace(bidOrder, order), unMatchedOrders + bidOrder)
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  val askPriceQuote: Option[Price] = (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
    case (Some(bidOrder), Some(askOrder)) => Some(bidOrder.limit max askOrder.limit)
    case (Some(bidOrder), None) => Some(bidOrder.limit)
    case (None, Some(askOrder)) => Some(askOrder.limit)
    case _ => None
  }

  val bidPriceQuote: Option[Price] = (unMatchedOrders.bidOrders.headOption, matchedOrders.askOrders.headOption) match {
    case (Some(bidOrder), Some(askOrder)) => Some(bidOrder.limit max askOrder.limit)
    case (Some(bidOrder), None) => Some(bidOrder.limit)
    case (None, Some(askOrder)) => Some(askOrder.limit)
    case _ => None
  }

  val spread: Option[Price] = {
    bidPriceQuote.flatMap(p1 => askPriceQuote.map(p2 => Price(p2.value - p1.value)))
  }

  def takeBestMatched: (Option[(LimitAskOrder[T], LimitBidOrder[T])], FourHeapOrderBook[T]) = {
    val (bestMatch, residualMatchedOrders) = matchedOrders.takeBestMatch
    bestMatch match {
      case result @ Some(_) => (result, new FourHeapOrderBook(residualMatchedOrders, unMatchedOrders))
      case None => (None, this)
    }
  }

  def takeAllMatched: (Stream[(LimitAskOrder[T], LimitBidOrder[T])], FourHeapOrderBook[T]) = {
    (matchedOrders.zipped, withEmptyMatchedOrders)
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

