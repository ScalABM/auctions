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

  val isEmpty: Boolean = matchedOrders.isEmpty && unMatchedOrders.isEmpty

  val nonEmpty: Boolean = matchedOrders.nonEmpty || unMatchedOrders.nonEmpty

  /** Remove an order from the `OrderBook`.
    *
    * @param uuid the universal unique identifier corresponding to the order that should be removed.
    * @return A new `OrderBook` with the order corresponding to the `uuid` removed.
    * @note Because multi-unit orders are divisible, the order with the `uuid` might have been split in which case it
    *       will exist in both the matched and unmatched order sets.
    */
  def - (uuid: UUID): FourHeapOrderBook[T] = {
    if (matchedOrders.contains(uuid) && unMatchedOrders.contains(uuid)) {
      val (residualMatched, additionalUnMatched) = matchedOrders - uuid
      val residualUnMatched = unMatchedOrders - uuid
      new FourHeapOrderBook(residualMatched, residualUnMatched.mergeWith(additionalUnMatched))
    } else if (matchedOrders.contains(uuid)) {
      val (residualMatched, additionalUnMatched) = matchedOrders - uuid
      new FourHeapOrderBook(residualMatched, unMatchedOrders.mergeWith(additionalUnMatched))
    } else {
      val residualUnMatched = unMatchedOrders - uuid
      new FourHeapOrderBook(matchedOrders, unMatchedOrders.mergeWith(residualUnMatched))
    }
  }

  /** Add a new `LimitAskOrder` to the `OrderBook`.
    *
    * @param uuid
    * @param order
    * @return
    * @note adding a new `LimitAskOrder` is non-trivial and there are several cases to consider.
    */
  def + (uuid: UUID, order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    (unMatchedOrders.bidOrders.headOption, matchedOrders.askOrders.headOption) match {
      case (Some((out, bidOrder)), Some((in, askOrder))) =>
        if (order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit) {
          val residualUnMatched = unMatchedOrders - out
          val (updatedMatched, optionalUnMatched) = matchedOrders + (uuid -> order, out -> bidOrder)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched)
          }
        } else if (order.limit <= askOrder.limit) {
          val (updatedMatched, additionalUnMatched) = matchedOrders.swap(uuid, order, in)
          new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (Some((out, bidOrder)), None) =>
        if (order.limit <= bidOrder.limit) {
          val (updatedMatched, optionalUnMatched) = matchedOrders + (uuid -> order, out -> bidOrder)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, unMatchedOrders)
          }
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (None, Some((in ,askOrder))) =>
        if (order.limit <= askOrder.limit) {
          val (updatedMatched, additionalUnMatched) = matchedOrders.swap(uuid, order, in)
          new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (None, None) => new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
    }
  }

  /** Add a new `LimitBidOrder` to the `OrderBook`.
    *
    * @param uuid
    * @param order
    * @return
    */
  def + (uuid: UUID, order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
      case (Some((in, bidOrder)), Some((out, askOrder))) =>
        if (order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit) {
          val residualUnMatched = unMatchedOrders - out
          val (updatedMatched, optionalUnMatched) = matchedOrders + (out -> askOrder, uuid -> order)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched)
          }
        } else if (order.limit >= bidOrder.limit) {
          val (updatedMatched, additionalUnMatched) = matchedOrders.swap(uuid, order, in)
          new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (Some((in ,bidOrder)), None) =>
        if (order.limit >= bidOrder.limit) {
          val (updatedMatched, additionalUnMatched) = matchedOrders.swap(uuid, order, in)
          new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (None, Some((out, askOrder))) =>
        if (order.limit >= askOrder.limit) {
          val (updatedMatched, optionalUnMatched) = matchedOrders + (out -> askOrder, uuid -> order)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, unMatchedOrders.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, unMatchedOrders)
          }
        } else {
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
        }
      case (None, None) => new FourHeapOrderBook(matchedOrders, unMatchedOrders + (uuid, order))
    }
  }

  def contains(uuid: UUID): Boolean = matchedOrders.contains(uuid) || unMatchedOrders.contains(uuid)

  def takeWhileMatched: (Stream[(LimitAskOrder[T], LimitBidOrder[T])], FourHeapOrderBook[T]) = {
    (matchedOrders.zipped, withEmptyMatchedOrders)
  }

  /** Update an existing `LimitAskOrder`.
    *
    * @note Because multi-unit orders are divisible, an order with the `uuid` might have been split in which case it
    *       will exist in both the matched and unmatched order sets. Thus if
    */
  def update(uuid: UUID, order: LimitAskOrder[T]): FourHeapOrderBook[T] = {
    if (contains(uuid)) {
      val residualOrderBook = this - uuid
      residualOrderBook + (uuid, order)
    } else {
      this + (uuid, order)
    }
  }

  /** Update an existing `LimitBidOrder`.
    *
    * @note Because multi-unit orders are divisible, an order with the `uuid` might have been split in which case it
    *       will exist in both the matched and unmatched order sets. Thus if
    */
  def update(uuid: UUID, order: LimitBidOrder[T]): FourHeapOrderBook[T] = {
    if (contains(uuid)) {
      val residualOrderBook = this - uuid
      residualOrderBook + (uuid, order)
    } else {
      this + (uuid, order)
    }
  }

  private[this] def withEmptyMatchedOrders: FourHeapOrderBook[T] = {
    val (askOrdering, bidOrdering) = (matchedOrders.askOrdering, matchedOrders.bidOrdering)
    new FourHeapOrderBook[T](MatchedOrders.empty(askOrdering, bidOrdering), unMatchedOrders)
  }

}


object FourHeapOrderBook {

  def empty[T <: Tradable](implicit askOrdering: Ordering[(UUID, LimitAskOrder[T])], bidOrdering: Ordering[(UUID, LimitBidOrder[T])]): FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty(askOrdering.reverse, bidOrdering.reverse)
    val unMatchedOrders = UnMatchedOrders.empty(askOrdering, bidOrdering)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

