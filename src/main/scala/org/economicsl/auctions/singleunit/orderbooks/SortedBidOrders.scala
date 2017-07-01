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
  * @param existing
  * @param sorted a heap of single-unit `BidOrder` instances.
  * @param numberUnits the total number of `Tradablle` units demanded by the issuers of the `BidOrder` instances
  *                    contained in this `SortedBidOrder` instance. Since this `SortedBidOrders` instance contains only
  *                    single-unit `BidOrder` instances, the `numberUnits` is also equal to the total number of
  *                    `BidOrder` instances.
  * @tparam T all `BidOrder` instances stored in the heap should be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class SortedBidOrders[T <: Tradable] private(existing: Map[Reference, (Token, BidOrder[T])],
                                                   sorted: immutable.TreeSet[(Reference, (Token, BidOrder[T]))],
                                                   val numberUnits: Quantity) {

  /** The ordering used to sort the `BidOrder` instances contained in this `SortedBidOrders` instance. */
  val ordering: Ordering[(Reference, (Token, BidOrder[T]))] = sorted.ordering

  /** Create a new `SortedBidOrders` instance containing the additional `BidOrder`.
    *
    * @param kv mapping between a unique identifier and an `BidOrder` that should be added.
    * @return a new `SortedBidOrder` instance that contains all of the `BidOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (Reference, (Token, BidOrder[T]))): SortedBidOrders[T] = {
    val (_, (_, order)) = kv
    new SortedBidOrders(existing + kv, sorted + kv, numberUnits + order.quantity)
  }

  /** Create a new `SortedBidOrders` instance with the given `AskOrder` removed from this `SortedBidOrders` instance.
    *
    * @param reference
    * @return
    */
  def - (reference: Reference): SortedBidOrders[T] = {
    existing.get(reference) match {
      case Some((token, order)) =>
        new SortedBidOrders(existing - reference, sorted - (reference -> (token -> order)), numberUnits - order.quantity)
      case None => // attempt to remove bid order that had already been processed!
        this
    }
  }

  /** Tests whether some `BidOrder` instance is contained in this `SortedBidOrders` instance.
    *
    * @param reference the `BidOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `SortedBidOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = existing.contains(reference)

  /** Selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    */
  def head: (Reference, (Token, BidOrder[T])) = sorted.head

  /** Optionally selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return Some `BidOrder` instance if this `SortedBidOrders` instance is non empty; `None` otherwise.
    */
  def headOption: Option[(Reference, (Token, BidOrder[T]))] = sorted.headOption

  /** Tests whether this `SortedBidOrder` instance is empty.
    *
    * @return `true` is there is no `BidOrder` instance in this `SortedBidOrders` instance; `false` otherwise.
    */
  def isEmpty: Boolean = sorted.isEmpty

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
