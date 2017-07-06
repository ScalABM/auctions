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

  /** If the constructor for `FourHeapOrderBook` becomes public, then this should be changed to require. */
  assert(orderBookInvariantsHold, ???)

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
    case (reference, incoming @ (_, order: AskOrder[T])) =>
      insert(reference, incoming)
    case (reference, incoming @ (_, order: BidOrder[T])) =>
      insert(reference, incoming)
  }

  /** Create a new `FourHeapOrderBook` with a given `AskOrder` removed from this order book.
    *
    * @param existing
    * @return
    * @note if `reference` is not found in this order book, then this order book is returned.
    */
  def remove(existing: Reference): (FourHeapOrderBook[T], Option[(Token, Order[T])]) = {
    val (remainingUnMatchedOrders, removedOrder) = unMatchedOrders - existing
    removedOrder match {
      case Some(_) =>
        (new FourHeapOrderBook(matchedOrders, remainingUnMatchedOrders), removedOrder)
      case None =>
        matchedOrders.get(existing) match {
          case Some((_, _: AskOrder[T])) =>
            val (_, (_, (_, bidOrder))) = matchedOrders.head
            unMatchedOrders.askOrders.headOption match {
              case Some(rationedAskOrder @ (reference, (_, askOrder))) if bidOrder.limit >= askOrder.limit =>
                val (remainingUnMatchedOrders, _) = unMatchedOrders - reference
                val (updatedMatchedOrders, removedAskOrder) = matchedOrders.replace(existing, rationedAskOrder)
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, Some(removedAskOrder))
              case _ =>
                val (remainingMatchedOrders, Some((removedAskOrder, marginalBidOrder))) = matchedOrders - existing
                val updatedUnMatchedOrders = unMatchedOrders + (??? -> marginalBidOrder)
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, Some(removedAskOrder))
            }
          case Some((_, _: BidOrder[T])) =>
            val ((_, (_, askOrder)), _) = matchedOrders.head
            unMatchedOrders.bidOrders.headOption match {
              case Some(rationedBidOrder @ (reference, (_, bidOrder))) if bidOrder.limit >= askOrder.limit =>
                val (updatedMatchedOrders, removedBidOrder) = matchedOrders.replace(existing, rationedBidOrder)
                val (remainingUnMatchedOrders, _) = unMatchedOrders - reference
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, Some(removedBidOrder))
              case _ =>
                val (remainingMatchedOrders, Some((marginalAskOrder, removedBidOrder))) = matchedOrders - existing
                val updatedUnMatchedOrders = unMatchedOrders + (??? -> marginalAskOrder)
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, Some(removedBidOrder))
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
  def splitAtTopMatch: (FourHeapOrderBook[T], Option[((Reference, (Token, AskOrder[T])), (Reference, (Token, BidOrder[T])))]) = {
    val (remainingMatchedOrders, topMatchedOrders) = matchedOrders.splitAtTopMatch
    (new FourHeapOrderBook(remainingMatchedOrders, unMatchedOrders), topMatchedOrders)
  }

  /** Create a new `FourHeapOrderBook` with an additional `AskOrder` (unless the ask order already exists in the order book).
    *
    * @param reference
    * @param kv
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `AskOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `AskOrder` instances contained in both the matched and unmatched sets.
    */
  private[this] def insert(reference: Reference, kv: (Token, AskOrder[T])): FourHeapOrderBook[T] = {
    val (_, askOrder) = kv
    (matchedOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(((_, (_, matchedAskOrder)), _)), Some((existing, rationedBidOrder @ (_, bidOrder))))
        if askOrder.limit <= bidOrder.limit && matchedAskOrder.limit <= bidOrder.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (reference -> kv, existing -> rationedBidOrder)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
      case (None, Some((existing, unMatchedBidOrder @ (_, bidOrder)))) if askOrder.limit < bidOrder.limit =>
        val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
        val updatedMatchedOrders = matchedOrders + (reference -> kv, existing -> unMatchedBidOrder)
        new FourHeapOrderBook(updatedMatchedOrders,remainingUnMatchedOrders)
      case (Some(((existing, (_, matchedAskOrder)), _)),  _) if askOrder.limit < matchedAskOrder.limit =>
        val (updatedMatchedOrders, matchedAskOrder) = matchedOrders.replace(existing, reference -> kv)
        new FourHeapOrderBook(updatedMatchedOrders, unMatchedOrders + (existing -> matchedAskOrder))
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + (reference -> kv))
    }
  }

  /** Create a new `FourHeapOrderBook` with an additional `BidOrder` (unless the ask order already exists in the order book).
    *
    * @param kv the `BidOrder` that should be added to this order book.
    * @return a new `FourHeapOrderBook` that contains all orders in this order book but that also contains `order`.
    * @note  Adding a new `BidOrder` to this order book should be an `O(log N)` where `N` is the total number of
    *        `BidOrder` instances contained in both the matched and unmatched sets.
    */
  private[this] def insert(reference: Reference, kv: (Token, BidOrder[T])): FourHeapOrderBook[T] = {
    val (token, order) = kv
    (matchedOrders.headOption, unMatchedOrders.askOrders.headOption) match {
      case (Some((_, (_, (_, matchedBidOrder)))), Some((existing, rationedAskOrder @ (_, askOrder))))
        if order.limit >= askOrder.limit && matchedBidOrder.limit >= askOrder.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (existing -> rationedAskOrder, reference -> kv)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
      case (None, Some((existing, unMatchedAskOrder @ (_, askOrder)))) if order.limit > askOrder.limit =>
        val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
        val updatedMatchedOrders = matchedOrders + (existing -> unMatchedAskOrder, reference -> kv)
        new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
      case (Some(((existing, (_, bidOrder)), _)), _) if order.limit > bidOrder.limit => // no rationing!
        val (updatedMatchedOrders, matchedBidOrder) = matchedOrders.replace(existing, reference -> kv)
        new FourHeapOrderBook(updatedMatchedOrders, unMatchedOrders + (existing -> matchedBidOrder))
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + (reference -> kv))
    }
  }

  /**
    *
    * @return true if order book invariants hold; false otherwise.
    */
  private[this] def orderBookInvariantsHold: Boolean = {
    matchedOrders.headOption.forall { case ((_, (_, a1)), (_, (_, b1))) =>
      unMatchedOrders.askOrders.headOption.forall { case (_, (_, a2)) =>
        unMatchedOrders.bidOrders.headOption.forall { case (_, (_, b2)) =>
          b1.limit >= b2.limit && a2.limit >= a1.limit
        }
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

