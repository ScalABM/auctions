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
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.core.{Quantity, Tradable}

import scala.collection.immutable


/** Class that stores a set of single-unit `BidOrder` instances in sorted order.
  *
  * @param orders
  * @param sortedOrders a heap of single-unit `BidOrder` instances.
  * @param numberUnits the total number of `Tradablle` units demanded by the issuers of the `BidOrder` instances
  *                    contained in this `SortedBidOrder` instance. Since this `SortedBidOrders` instance contains only
  *                    single-unit `BidOrder` instances, the `numberUnits` is also equal to the total number of
  *                    `BidOrder` instances.
  * @tparam T all `BidOrder` instances stored in the heap should be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class SortedBidOrders[T <: Tradable] private(orders: Map[Reference, (Token, BidOrder[T])],
                                                   sortedOrders: immutable.TreeSet[(Reference, (Token, BidOrder[T]))],
                                                   val numberUnits: Quantity) {

  /** The ordering used to sort the `BidOrder` instances contained in this `SortedBidOrders` instance. */
  val ordering: Ordering[(Reference, (Token, BidOrder[T]))] = sortedOrders.ordering

  /** Create a new `SortedBidOrders` instance containing the additional `BidOrder`.
    *
    * @param kv mapping between a unique identifier and an `BidOrder` that should be added.
    * @return a new `SortedBidOrder` instance that contains all of the `BidOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (Reference, (Token, BidOrder[T]))): SortedBidOrders[T] = {
    val (_, (_, order)) = kv
    new SortedBidOrders(orders + kv, sortedOrders + kv, numberUnits + order.quantity)
  }

  /** Create a new `SortedBidOrders` instance with the given `AskOrder` removed from this `SortedBidOrders` instance.
    *
    * @param reference
    * @return
    */
  def - (reference: Reference): (SortedBidOrders[T], Option[(Token, BidOrder[T])]) = {
    orders.get(reference) match {
      case Some(kv @ (_, order)) =>
        val remainingOrders = orders - reference
        val remainingSortedOrders = sortedOrders - (reference -> kv)
        val remainingUnits = numberUnits - order.quantity
        (new SortedBidOrders(remainingOrders, remainingSortedOrders, remainingUnits), Some(kv))
      case None =>  // attempt to remove ask order that had already been processed!
        (this, None)
    }
  }

  /** Tests whether some `BidOrder` instance is contained in this `SortedBidOrders` instance.
    *
    * @param reference the `BidOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `SortedBidOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = orders.contains(reference)

  def get(reference: Reference): Option[(Token, BidOrder[T])] = {
    orders.get(reference)
  }

  /** Selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    */
  def head: (Reference, (Token, BidOrder[T])) = sortedOrders.head

  /** Optionally selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return Some `BidOrder` instance if this `SortedBidOrders` instance is non empty; `None` otherwise.
    */
  def headOption: Option[(Reference, (Token, BidOrder[T]))] = sortedOrders.headOption

  /** Tests whether this `SortedBidOrder` instance is empty.
    *
    * @return `true` is there is no `BidOrder` instance in this `SortedBidOrders` instance; `false` otherwise.
    */
  def isEmpty: Boolean = sortedOrders.isEmpty

  def splitOffTopOrder: (SortedBidOrders[T], Option[(Reference, (Token, BidOrder[T]))]) = {
    headOption match {
      case Some((reference, (_, askOrder))) =>
        val remainingOrders = orders - reference
        val remainingUnits = numberUnits - askOrder.quantity
        (new SortedBidOrders(remainingOrders, sortedOrders.tail, remainingUnits), headOption)
      case None =>
        (this, None)
    }
  }

  def tail: SortedBidOrders[T] = {
    ???
  }

}


/** Companion object for `SortedBidOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedBidOrders {

  /** Create an instance of `SortedBidOrders` that does not contain any `BidOrder` instances.
    *
    * @param ordering the ordering used to sort the underlying heap of `BidOrder` instances.
    * @tparam T all `BidOrder` instances stored in the heap should be for the same type of `Tradable`.
    * @return an instance of `SortedBidOrders`.
    */
  def empty[T <: Tradable](ordering: Ordering[BidOrder[T]]): SortedBidOrders[T] = {
    val existing = immutable.HashMap.empty[Reference, (Token, BidOrder[T])]
    val sorted = immutable.TreeSet.empty[(Reference, (Token, BidOrder[T]))](???) // todo need to convert ordering!
    new SortedBidOrders(existing, sorted, Quantity.zero)
  }

}
