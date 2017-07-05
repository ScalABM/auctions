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
  * @param matchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                      that comprise the current matched set.
  * @param unMatchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                        that comprise the current unmatched set.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `FourHeapOrderBook` should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  * @note Implementation taken from Wurman et al (1998). Algorithm uses four heaps to organize the submitted orders.
  *       Orders are distinguished by whether or not they are `AskOrder` or `BidOrder` instances, and whether or not
  *       they are in the current matched set.
  */
final class FourHeapOrderBook[T <: Tradable] private(val matchedOrders: MatchedOrders[T],
                                                     val unMatchedOrders: UnMatchedOrders[T]) {

  require(orderBookInvariantsHold)

  /** The ask price quote is the price that a buyer would need to exceed in order for its bid to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The ask price quote should be equal to the Mth highest price (where M is the total number of ask orders in
    *       the order book). The ask price quote should be undefined if there are no ask orders in the order book.
    */
  def askPriceQuote: Option[Price] = (matchedOrders.headOption, unMatchedOrders.askOrders.headOption) match {
    case (Some((_, (_, (_, bidOrder)))), Some((_, (_, askOrder)))) =>
      Some(bidOrder.limit min askOrder.limit)  // askOrder might have been rationed!
    case (Some((_, (_, (_, bidOrder)))), None) =>
      Some(bidOrder.limit)
    case (None, Some((_, (_, askOrder)))) =>
      Some(askOrder.limit)
    case (None, None) =>
      None
  }

  /** The bid price quote is the price that a seller would need to beat in order for its offer to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The bid price quote should be equal to the (M+1)th highest price (where M is the total number of ask orders
    *       in the order book). The bid price quote should be undefined if there are no bid orders in the order book.
    */
  def bidPriceQuote: Option[Price] = (matchedOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
    case (Some(((_, (_, askOrder)), _)), Some((_, (_, bidOrder)))) =>
      Some(bidOrder.limit max askOrder.limit)  // bidOrder might have been rationed!
    case (Some(((_, (_, askOrder)), _)), None) =>
      Some(askOrder.limit)
    case (None, Some((_, (_, bidOrder)))) =>
      Some(bidOrder.limit)
    case (None, None) =>
      None
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
    val (remainingUnMatchedOrders, removedUnMatchedOrder) = unMatchedOrders - existing
    removedUnMatchedOrder match {
      case Some(_) =>
        (new FourHeapOrderBook(matchedOrders, remainingUnMatchedOrders), removedUnMatchedOrder)
      case None =>
        matchedOrders.get(existing) match {
          case removedOrder @ Some((_, _: AskOrder[T])) =>
            val (_, (_, bidOrder)) = matchedOrders.headOption
            unMatchedOrders.askOrders.headOption match {
              case Some(rationedAskOrder @ (reference, (_, askOrder))) if bidOrder.limit >= askOrder.limit =>
                val updatedMatchedOrders = matchedOrders.replace(existing, rationedAskOrder)
                val remainingUnMatchedOrders = unMatchedOrders - reference
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, removedOrder)
              case _ =>
                val marginalBidOrder @ (reference, _) = matchedOrders.bidOrders.head
                val remainingMatchedOrders = matchedOrders - (existing, reference)
                val updatedUnMatchedOrders = unMatchedOrders + marginalBidOrder
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removedOrder)
            }
          case removedOrder @ Some((_, _: BidOrder[T])) =>
            val (_, (_, askOrder)) = matchedOrders.askOrders.head
            unMatchedOrders.bidOrders.headOption match {
              case Some(rationedBidOrder @ (reference, (_, bidOrder))) if bidOrder.limit >= askOrder.limit =>
                val updatedMatchedOrders = matchedOrders.replace(existing, rationedBidOrder)
                val remainingUnMatchedOrders = unMatchedOrders - reference
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, removedOrder)
              case _ =>
                val marginalAskOrder @ (reference, _) = matchedOrders.askOrders.head
                val remainingMatchedOrders = matchedOrders - (existing, reference)
                val updatedUnMatchedOrders = unMatchedOrders + marginalAskOrder
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, removedOrder)
          case None =>
            (this, None)
        }
      }
    }
  }

  def - (reference: Reference): (FourHeapOrderBook[T], Option[(Token, Order[T])]) = {
    if (unMatchedOrders.contains(reference)) {
      val (remainingUnMatchedOrders, removedUnMatchedOrder) = unMatchedOrders - reference
      (new FourHeapOrderBook(matchedOrders, remainingUnMatchedOrders), removedUnMatchedOrder)
    } else if (matchedOrders.contains(reference)) {
      matchedOrders.get(reference) match {
        case (_, (_, order: AskOrder[T])) =>
        case (_, (_, order: BidOrder[T])) =>
      }
    } else {
      (this, None)
    }
  }

  /** Split this `FourHeapOrderBook` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `FourHeapOrderBook` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if the underlying
    *         `MatchedOrders` instance is non-empty (first element is `None` otherwise), and whose second element is
    *         the residual `FourHeapOrderBook` instance.
    */
  def splitAtTopMatch: (FourHeapOrderBook[T], Option[((Reference, (Token, AskOrder[T])), (Reference, (Token, BidOrder[T])))]) = {
    val (remainingMatchedOrders, topMatchedOrders) = matchedOrders.splitAtTopMatch
    (new FourHeapOrderBook(remainingMatchedOrders, unMatchedOrders), topMatchedOrders)
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
    (matchedOrders.headOption, unMatchedOrders.headOption) match {
      case (Some(((_, (_, askOrder)), _)), Some((_, (existing, (_, bidOrder))))) if order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit => // bidOrder was rationed!
        val (remainingUnMatchedOrders, Some(removedBidOrder)) = unMatchedOrders - existing
        val updatedMatchedOrders = matchedOrders + (reference -> order, existing -> removedBidOrder)
        new FourHeapOrderBook(matchedOrders + (reference -> order, existing -> bidOrder), remainingUnMatchedOrders)
      case (None, Some((existing, bidOrder))) if order.limit < bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (reference -> order, existing -> bidOrder), unMatchedOrders - existing)
      case (Some((existing, askOrder)), _) if order.limit < askOrder.limit =>
        new FourHeapOrderBook(matchedOrders.replace(existing, reference -> order), unMatchedOrders + (existing -> askOrder))
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + (reference -> order))
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
    (matchedOrders.headOption, unMatchedOrders.headOption) match {
      case (Some((_, bidOrder)), Some((existing, askOrder))) if order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit => // askOrder was rationed!
        new FourHeapOrderBook(matchedOrders + (existing -> askOrder, reference -> order), unMatchedOrders - existing)
      case (None, Some((existing, askOrder))) if order.limit > askOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (existing -> askOrder, reference -> order), unMatchedOrders - existing)
      case (Some((existing, bidOrder)), _) if order.limit > bidOrder.limit => // no rationing!
        new FourHeapOrderBook(matchedOrders.replace(existing, reference -> order), unMatchedOrders + (existing -> bidOrder))
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + (reference -> order))
    }
  }

  /**
    *
    * @return true if order book invariants hold; false otherwise.
    */
  private[this] def orderBookInvariantsHold: Boolean = {
    matchedOrders.headOption.forall{ case ((_, (_, a1)), (_, (_, b1))) =>
      unMatchedOrders.headOption.forall{ case ((_, (_, a2)), (_, (_, b2))) =>
        b1.limit >= b2.limit && a2.limit >= a1.limit
      }
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

