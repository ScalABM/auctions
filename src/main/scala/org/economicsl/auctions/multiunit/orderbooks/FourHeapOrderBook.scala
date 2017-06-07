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

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.multiunit.orders.{AskOrder, BidOrder}


class FourHeapOrderBook[K, T <: Tradable] private(matched: MatchedOrders[K, T], unMatched: UnMatchedOrders[K, T]) {

  require(matched.bidOrders.headOption.forall{ case (_, b1) => unMatched.bidOrders.headOption.forall{ case (_, b2) => b1.limit >= b2.limit } })

  require(unMatched.askOrders.headOption.forall{ case (_, a1) => matched.askOrders.headOption.forall{ case (_, a2) => a1.limit >= a2.limit } })

  /*
  /** Add a new `AskOrder` to the `OrderBook`.
    *
    * @param key
    * @param order
    * @return
    * @note adding a new `AskOrder` is non-trivial and there are several cases to consider.
    */
  def + (key: K, order: AskOrder[T]): FourHeapOrderBook[K, T] = {
    (unMatched.bidOrders.headOption, matched.askOrders.headOption) match {
      case (Some((out, bidOrder)), Some((in, askOrder))) =>
        if (order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit) {
          val remainingUnMatched = unMatched - (out, bidOrder)
          val (updatedMatched, additionalUnMatched) = matched + (key -> order, out -> bidOrder)
          val default = new FourHeapOrderBook(updatedMatched, remainingUnMatched)
          additionalUnMatched.fold(default)(orders => new FourHeapOrderBook(updatedMatched, remainingUnMatched.mergeWith(orders)))
        } else if (order.limit <= askOrder.limit) {
          val (updatedMatched, additionalUnMatched) = matched - (in, askOrder)  // what happens if queue is empty?
          updatedMatched + (key -> order, ???) additionalUnMatched.bidOrders
          new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
          ???
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (Some((out, bidOrder)), None) =>
        if (order.limit <= bidOrder.limit) {
          val (updatedMatched, optionalUnMatched) = matched + (key -> order, out -> bidOrder)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, unMatched)
          }
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (None, Some((in ,askOrder))) =>
        if (order.limit <= askOrder.limit) {
          //val (updatedMatched, additionalUnMatched) = matched.swap(key -> order, in)
          //new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
          ???
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (None, None) => new FourHeapOrderBook(matched, unMatched + (key, order))
    }
  }

  /** Add a new `BidOrder` to the `OrderBook`.
    *
    * @param key
    * @param order
    * @return
    */
  def + (key: K, order: BidOrder[T]): FourHeapOrderBook[K, T] = {
    (matched.bidOrders.headOption, unMatched.askOrders.headOption) match {
      case (Some((in, bidOrder)), Some((out, askOrder))) =>
        if (order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit) {
          val residualUnMatched = unMatched - out
          val (updatedMatched, optionalUnMatched) = matched + (out -> askOrder, key -> order)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, residualUnMatched)
          }
        } else if (order.limit >= bidOrder.limit) {
          //val (updatedMatched, additionalUnMatched) = matched.swap(key -> order, in)
          //new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
          ???
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (Some((in ,bidOrder)), None) =>
        if (order.limit >= bidOrder.limit) {
          //val (updatedMatched, additionalUnMatched) = matched.swap(key -> order, in)
          //new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
          ???
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (None, Some((out, askOrder))) =>
        if (order.limit >= askOrder.limit) {
          val (updatedMatched, optionalUnMatched) = matched + (out -> askOrder, key -> order)
          optionalUnMatched match {
            case Some(additionalUnMatched) =>
              new FourHeapOrderBook(updatedMatched, unMatched.mergeWith(additionalUnMatched))
            case None =>
              new FourHeapOrderBook(updatedMatched, unMatched)
          }
        } else {
          new FourHeapOrderBook(matched, unMatched + (key, order))
        }
      case (None, None) => new FourHeapOrderBook(matched, unMatched + (key, order))
    }
  }

  def contains(key: K): Boolean = matched.contains(key) || unMatched.contains(key)

  def isEmpty: Boolean = matched.isEmpty && unMatched.isEmpty

  def nonEmpty: Boolean = matched.nonEmpty || unMatched.nonEmpty

  /*def - (key: K): FourHeapOrderBook[K, T] = {
    if (matched.contains(key) && unMatched.contains(key)) {
      val (residualMatched, additionalUnMatched) = matched - key
      val residualUnMatched = unMatched - key
      new FourHeapOrderBook(residualMatched, residualUnMatched.mergeWith(additionalUnMatched))
    } else if (matched.contains(key)) {
      val (residualMatched, additionalUnMatched) = matched - key
      new FourHeapOrderBook(residualMatched, unMatched.mergeWith(additionalUnMatched))
    } else {
      val residualUnMatched = unMatched - key
      new FourHeapOrderBook(matched, unMatched.mergeWith(residualUnMatched))
    }
  }

  /** Update an existing `AskOrder`.
    *
    * @note If an `AskOrder[T]` associated with the `key` already exists in the `OrderBook`, then the existing order
    *       (or orders in the case that the previously submitted order was split) are removed from this `OrderBook`
    *       before the `order` is added to this `OrderBook`.
    */
  def updated(key: K, order: AskOrder[T]): FourHeapOrderBook[K, T] = {
    if (contains(key)) this - key + (key -> order) else this + (key -> order)
  }

  /** Update an existing `BidOrder`.
    *
    * @note If an `AskOrder[T]` associated with the `key` already exists in the `OrderBook`, then the existing order
    *       (or orders in the case that the previously submitted order was split) are removed from this `OrderBook`
    *       before the `order` is added to this `OrderBook`.
    */
  def updated(key: K, order: BidOrder[T]): FourHeapOrderBook[K, T] = {
    if (contains(key)) this - key + (key -> order) else this + (key -> order)
  }*/

  */
}


object FourHeapOrderBook {

  def empty[K, T <: Tradable](implicit askOrdering: Ordering[K], bidOrdering: Ordering[K]): FourHeapOrderBook[K, T] = {
    val matchedOrders = MatchedOrders.empty[K, T](askOrdering.reverse, bidOrdering)
    val unMatchedOrders = UnMatchedOrders.empty[K, T](askOrdering, bidOrdering.reverse)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }


}

