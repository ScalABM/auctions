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
package org.economicsl.auctions.singleunit.orderbooks

import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.auctions.{Price, Tradable}


/** Class implementing the four-heap order book algorithm.
  *
  * @param matchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                      that comprise the current matched set.
  * @param unMatchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                        that comprise the current unmatched set.
  * @tparam T orders inserted into the order book must be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  * @note Implementation taken from Wurman et al (1998). Algorithm uses four heaps to organize the submitted orders.
  *       Orders are distinguished by whether or not they are buy or sell orders, and whether or not they are in the
  *       current matched set.
  */
class FourHeapOrderBook[T <: Tradable] private(val matchedOrders: MatchedOrders[T], val unMatchedOrders: UnMatchedOrders[T]) {

  require(matchedOrders.bidOrders.headOption.forall(b1 => unMatchedOrders.bidOrders.headOption.forall(b2 => b1.limit >= b2.limit)))

  require(unMatchedOrders.askOrders.headOption.forall(a1 => matchedOrders.askOrders.headOption.forall(a2 => a1.limit >= a2.limit)))

  /** The ask price quote is the price that a buyer would need to exceed in order for its bid to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The ask price quote should be equal to the Mth highest price (where M is the total number of ask orders in
    *       the order book). The ask price quote should be undefined if there are no ask orders in the order book.
    */
  def askPriceQuote: Option[Price] = (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
    case (Some(bidOrder), Some(askOrder)) => Some(bidOrder.limit min askOrder.limit)  // askOrder might have been rationed!
    case (Some(bidOrder), None) => Some(bidOrder.limit)
    case (None, Some(askOrder)) => Some(askOrder.limit)
    case (None, None) => None
  }

  /** The bid price quote is the price that a seller would need to beat in order for its offer to be matched had the
    *  auction cleared at the time the quote was issued.
    *
    * @note The bid price quote should be equal to the (M+1)th highest price (where M is the total number of ask orders
    *       in the order book). The bid price quote should be undefined if there are no bid orders in the order book.
    */
  def bidPriceQuote: Option[Price] = (unMatchedOrders.bidOrders.headOption, matchedOrders.askOrders.headOption) match {
    case (Some(bidOrder), Some(askOrder)) => Some(bidOrder.limit max askOrder.limit)  // bid Order might have been rationed!
    case (Some(bidOrder), None) => Some(bidOrder.limit)
    case (None, Some(askOrder)) => Some(askOrder.limit)
    case (None, None) => None
  }

  def spread: Option[Price] = {
    bidPriceQuote.flatMap(bidPrice => askPriceQuote.map(askPrice => Price(bidPrice.value - askPrice.value)))
  }

  /** Create a new `FourHeapOrderBook` with an additional `AskOrder` (unless the ask order already exists in the order book).
    *
    * @param order the `AskOrder` that should be added to this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `AskOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `AskOrder` instances contained in both the matched and unmatched sets.
    */
  def insert(order: AskOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) =>
        if (order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit) {  // bidOrder was rationed!
          new FourHeapOrderBook(matchedOrders + ((order, bidOrder)), unMatchedOrders - bidOrder)
        } else if (order.limit < askOrder.limit) {
          new FourHeapOrderBook(matchedOrders.replace(askOrder, order), unMatchedOrders + askOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (None, Some(bidOrder)) =>
        if (order.limit < bidOrder.limit) {
          new FourHeapOrderBook(matchedOrders + ((order, bidOrder)), unMatchedOrders - bidOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (Some(askOrder), None) =>
        if ( order.limit < askOrder.limit) {
          new FourHeapOrderBook(matchedOrders.replace(askOrder, order), unMatchedOrders + askOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (None, None) =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  /** Create a new `FourHeapOrderBook` with an additional `BidOrder` (unless the ask order already exists in the order book).
    *
    * @param order the `BidOrder` that should be added to this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `BidOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `BidOrder` instances contained in both the matched and unmatched sets.
    */
  def insert(order: BidOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
      case (Some(bidOrder), Some(askOrder)) =>
        if (order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit) { // askOrder was rationed!
          new FourHeapOrderBook(matchedOrders + ((askOrder, order)), unMatchedOrders - askOrder)
        } else if (order.limit > bidOrder.limit) { // no rationing!
          new FourHeapOrderBook(matchedOrders.replace(bidOrder, order), unMatchedOrders + bidOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (None, Some(askOrder)) =>
        if (order.limit > askOrder.limit) {
          new FourHeapOrderBook(matchedOrders + ((askOrder, order)), unMatchedOrders - askOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (Some(bidOrder), None) =>
        if (order.limit > bidOrder.limit) {
          new FourHeapOrderBook(matchedOrders.replace(bidOrder, order), unMatchedOrders + bidOrder)
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
        }
      case (None, None) =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  /** Create a new `FourHeapOrderBook` with a given `AskOrder` removed from this order book.
    *
    * @param order the `AskOrder` that should be removed from this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that does not contain `order`.
    * @note if `order` is not found in this order book, then this order book is returned. Removing a previously inserted
    *       `AskOrder` from this order book should be an `O(log N)` where `N` is the total number of `AskOrder`
    *       instances contained in both the matched and unmatched sets.
    */
  def remove(order: AskOrder[T]): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else if (matchedOrders.contains(order)) {
      val bidOrder = matchedOrders.bidOrders.head
      unMatchedOrders.askOrders.headOption match {
        case Some(askOrder) if askOrder.limit <= bidOrder.limit =>  // askOrder was rationed!
          new FourHeapOrderBook(matchedOrders.replace(order, askOrder), unMatchedOrders - askOrder)
        case _ => new FourHeapOrderBook(matchedOrders - ((order, bidOrder)), unMatchedOrders + bidOrder)
      }
    } else {
      this
    }
  }

  /** Create a new `FourHeapOrderBook` with a given `BidOrder` removed from this order book.
    *
    * @param order the `BidOrder` that should be removed from this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that does not contain `order`.
    * @note if `order` is not found in this order book, then this order book is returned. Removing a previously inserted
    *       `BidOrder` from this order book should be an `O(log N)` where `N` is the total number of `BidOrder`
    *       instances contained in both the matched and unmatched sets.
    */
  def remove(order: BidOrder[T]): FourHeapOrderBook[T] = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else if (matchedOrders.contains(order)) {
      val askOrder = matchedOrders.askOrders.head
      unMatchedOrders.bidOrders.headOption match {
        case Some(bidOrder) if bidOrder.limit >= askOrder.limit =>  // bidOrder was rationed!
          new FourHeapOrderBook(matchedOrders.replace(order, bidOrder), unMatchedOrders - bidOrder)
        case _ => new FourHeapOrderBook(matchedOrders - ((askOrder, order)), unMatchedOrders + askOrder)
      }
    } else {
      this
    }
  }

  def takeBestMatched: (Option[(AskOrder[T], BidOrder[T])], FourHeapOrderBook[T]) = {
    val (bestMatch, residualMatchedOrders) = matchedOrders.takeBestMatch
    bestMatch match {
      case result @ Some(_) => (result, new FourHeapOrderBook(residualMatchedOrders, unMatchedOrders))
      case None => (None, this)
    }
  }

}


/** Companion object for `FourHeapOrderBook`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object FourHeapOrderBook {

  def empty[T <: Tradable]: FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty[T](Order.ordering.reverse, Order.ordering)
    val unMatchedOrders = UnMatchedOrders.empty[T](Order.ordering, Order.ordering.reverse)
    new FourHeapOrderBook[T](matchedOrders, unMatchedOrders)
  }

  def empty[T <: Tradable](askOrdering: Ordering[AskOrder[T]], bidOrdering: Ordering[BidOrder[T]]): FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty(askOrdering.reverse, bidOrdering)
    val unMatchedOrders = UnMatchedOrders.empty(askOrdering, bidOrdering.reverse)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

