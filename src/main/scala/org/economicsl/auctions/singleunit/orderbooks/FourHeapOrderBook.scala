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

import org.economicsl.auctions.{Reference, Token}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.core.{Currency, Price, Tradable}


/** Class implementing the four-heap order book algorithm.
  *
  * @param matched contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances, that
  *                comprise the current matched set.
  * @param unMatched contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances, that
  *                  comprise the current unmatched set.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `FourHeapOrderBook` should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  * @note Implementation taken from Wurman et al (1998). Algorithm uses four heaps to organize the submitted orders.
  *       Orders are distinguished by whether or not they are `AskOrder` or `BidOrder` instances, and whether or not
  *       they are in the current matched set.
  */
final class FourHeapOrderBook[T <: Tradable] private(val matched: MatchedOrders[T], val unMatched: UnMatchedOrders[T]) {

  require(matched.bidOrders.headOption.forall{ case (_, (_, b1)) => unMatched.bidOrders.headOption.forall{ case (_, (_, b2)) => b1.limit >= b2.limit } })
  require(unMatched.askOrders.headOption.forall{ case (_, (_, a1)) => matched.askOrders.headOption.forall{ case (_, (_, a2)) => a1.limit >= a2.limit } })

  def askOrderIsRationed: Boolean = {
    ???
  }

  def bidOrderIsRationed: Boolean = {
    ???
  }

  /** The ask price quote is the price that a buyer would need to exceed in order for its bid to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The ask price quote should be equal to the Mth highest price (where M is the total number of ask orders in
    *       the order book). The ask price quote should be undefined if there are no ask orders in the order book.
    */
  def askPriceQuote: Option[Price] = (matched.bidOrders.headOption, unMatched.askOrders.headOption) match {
    case (Some((_, (_, bidOrder))), Some((_, (_, askOrder)))) => Some(bidOrder.limit min askOrder.limit)  // askOrder might have been rationed!
    case (Some((_, (_, bidOrder))), None) => Some(bidOrder.limit)
    case (None, Some((_, (_, askOrder)))) => Some(askOrder.limit)
    case (None, None) => None
  }

  /** The bid price quote is the price that a seller would need to beat in order for its offer to be matched had the
    *  auction cleared at the time the quote was issued.
    *
    * @note The bid price quote should be equal to the (M+1)th highest price (where M is the total number of ask orders
    *       in the order book). The bid price quote should be undefined if there are no bid orders in the order book.
    */
  def bidPriceQuote: Option[Price] = (unMatched.bidOrders.headOption, matched.askOrders.headOption) match {
    case (Some((_, (_, bidOrder))), Some((_, (_, askOrder)))) => Some(bidOrder.limit max askOrder.limit)
    case (Some((_, (_, bidOrder))), None) => Some(bidOrder.limit)
    case (None, Some((_, (_, askOrder)))) => Some(askOrder.limit)
    case (None, None) => None
  }

  def spread: Option[Currency] = {
    bidPriceQuote.flatMap(bidPrice => askPriceQuote.map(askPrice => bidPrice.value - askPrice.value))
  }

  def insert(kv: (Reference, (Token, Order[T]))): FourHeapOrderBook[T] = kv match {
    case (reference, (_, order: AskOrder[T])) =>
      insert(reference, order)
    case (reference, (_, order: BidOrder[T])) =>
      insert(reference, order)
  }

  /** Create a new `FourHeapOrderBook` with a given `AskOrder` removed from this order book.
    *
    * @param existing
    * @return
    * @note if `reference` is not found in this order book, then this order book is returned.
    */
  def remove(existing: Reference): (FourHeapOrderBook[T], Option[(Token, Order[T])]) = {
    unMatched.get(existing) match {
      case removed @ Some(_) =>
        (new FourHeapOrderBook(matched, unMatched - existing), removed)
      case None =>
        matched.get(existing) match {
          case removed @ Some((_, _: AskOrder[T])) =>
            val (_, (_, bidOrder)) = matched.bidOrders.head
            unMatched.askOrders.headOption match {
              case Some(rationedAskOrder @ (reference, (_, askOrder))) if bidOrder.limit >= askOrder.limit =>
                val updatedMatchedOrders = matched.replace(existing, rationedAskOrder)
                val updatedUnMatchedOrders = unMatched - reference
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removed)
              case _ =>
                val marginalBidOrder @ (reference, _) = matched.bidOrders.head
                val updatedMatchedOrders = matched - (existing, reference)
                val updatedUnMatchedOrders = unMatched + marginalBidOrder
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removed)
            }
          case removed @ Some((_, _: BidOrder[T])) =>
            val (_, (_, askOrder)) = matched.askOrders.head
            unMatched.bidOrders.headOption match {
              case Some(rationedBidOrder @ (reference, (_, bidOrder))) if bidOrder.limit >= askOrder.limit =>
                val updatedMatchedOrders = matched.replace(existing, rationedBidOrder)
                val updatedUnMatchedOrders = unMatched - reference
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removed)
              case _ =>
                val marginalAskOrder @ (reference, _) = matched.askOrders.head
                val updatedMatchedOrders = matched - (existing, reference)
                val updatedUnMatchedOrders = unMatched + marginalAskOrder
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removed)
          case None =>
            (this, None)
        }
      }
    }
  }

  /** Split this `FourHeapOrderBook` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `FourHeapOrderBook` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if the underlying
    *         `MatchedOrders` instance is non-empty (first element is `None` otherwise), and whose second element is
    *         the residual `FourHeapOrderBook` instance.
    */
  def splitAtBestMatch: (Option[(AskOrder[T], BidOrder[T])], FourHeapOrderBook[T]) = {
    val (bestMatch, residual) = matched.splitAtBestMatch
    bestMatch match {
      case result @ Some(_) => (result, new FourHeapOrderBook(residual, unMatched))
      case None => (None, this)
    }
  }

  /** Create a new `FourHeapOrderBook` with an additional `AskOrder` (unless the ask order already exists in the order book).
    *
    * @param reference
    * @param order the `AskOrder` that should be added to this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `AskOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `AskOrder` instances contained in both the matched and unmatched sets.
    */
  private[this] def insert(reference: Reference, order: AskOrder[T]): FourHeapOrderBook[T] = {
    (matched.askOrders.headOption, unMatched.bidOrders.headOption) match {
      case (Some((_, askOrder)), Some((existing, bidOrder))) if order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit => // bidOrder was rationed!
        new FourHeapOrderBook(matched + (reference -> order, existing -> bidOrder), unMatched - existing)
      case (None, Some((existing, bidOrder))) if order.limit < bidOrder.limit =>
        new FourHeapOrderBook(matched + (reference -> order, existing -> bidOrder), unMatched - existing)
      case (Some((existing, askOrder)), _) if order.limit < askOrder.limit =>
        new FourHeapOrderBook(matched.replace(existing, reference -> order), unMatched + (existing -> askOrder))
      case _ =>
        new FourHeapOrderBook(matched, unMatched + (reference -> order))
    }
  }

  /** Create a new `FourHeapOrderBook` with an additional `BidOrder` (unless the ask order already exists in the order book).
    *
    * @param order the `BidOrder` that should be added to this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `BidOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `BidOrder` instances contained in both the matched and unmatched sets.
    */
  private[this] def insert(reference: Reference, order: BidOrder[T]): FourHeapOrderBook[T] = {
    (matched.bidOrders.headOption, unMatched.askOrders.headOption) match {
      case (Some((_, bidOrder)), Some((existing, askOrder))) if order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit => // askOrder was rationed!
        new FourHeapOrderBook(matched + (existing -> askOrder, reference -> order), unMatched - existing)
      case (None, Some((existing, askOrder))) if order.limit > askOrder.limit =>
        new FourHeapOrderBook(matched + (existing -> askOrder, reference -> order), unMatched - existing)
      case (Some((existing, bidOrder)), _) if order.limit > bidOrder.limit => // no rationing!
        new FourHeapOrderBook(matched.replace(existing, reference -> order), unMatched + (existing -> bidOrder))
      case _ =>
        new FourHeapOrderBook(matched, unMatched + (reference -> order))
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

