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

import org.economicsl.auctions.OrderBook
import org.economicsl.auctions.messages.{OrderId, OrderReferenceId, SingleUnitBid}
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
final class SortedSingleUnitBids[T <: Tradable] private(
  orders: Map[OrderReferenceId, SingleUnitBid[T]],
  sortedOrders: immutable.TreeSet[(OrderReferenceId, SingleUnitBid[T])],
  val numberUnits: Quantity)
    extends OrderBook[T, SingleUnitBid[T], SortedSingleUnitBids[T]] {

  /** The ordering used to sort the `BidOrder` instances contained in this `SortedBidOrders` instance. */
  val ordering: Ordering[(OrderReferenceId, SingleUnitBid[T])] = sortedOrders.ordering

  /** Create a new `SortedBidOrders` instance containing the additional `BidOrder`.
    *
    * @param kv mapping between a unique identifier and an `BidOrder` that should be added.
    * @return a new `SortedBidOrder` instance that contains all of the `BidOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (OrderReferenceId, SingleUnitBid[T])): SortedSingleUnitBids[T] = {
    val (_, (_, order)) = kv
    new SortedSingleUnitBids(orders + kv, sortedOrders + kv, numberUnits + order.quantity)
  }

  /** Create a new `SortedBidOrders` instance with the given `AskOrder` removed from this `SortedBidOrders` instance.
    *
    * @param orderRefId
    * @return
    */
  def - (orderRefId: OrderReferenceId): (SortedSingleUnitBids[T], Option[(OrderId, SingleUnitBid[T])]) = {
    orders.get(orderRefId) match {
      case Some(kv @ (_, order)) =>
        val remainingOrders = orders - orderRefId
        val remainingSortedOrders = sortedOrders - (orderRefId -> kv)
        val remainingUnits = numberUnits - order.quantity
        (new SortedSingleUnitBids(remainingOrders, remainingSortedOrders, remainingUnits), Some(kv))
      case None =>  // attempt to remove ask order that had already been processed!
        (this, None)
    }
  }

  /** Tests whether some `BidOrder` instance is contained in this `SortedBidOrders` instance.
    *
    * @param orderRefId the `BidOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `SortedBidOrders` instance; `false` otherwise.
    */
  def contains(orderRefId: OrderReferenceId): Boolean = orders.contains(orderRefId)

  def get(orderRefId: OrderReferenceId): Option[(OrderId, SingleUnitBid[T])] = {
    orders.get(orderRefId)
  }

  /** Selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    */
  def head: (OrderReferenceId, (OrderId, SingleUnitBid[T])) = sortedOrders.head

  /** Optionally selects the first `BidOrder` instance contained in this `SortedBidOrders` instance.
    *
    * @return Some `BidOrder` instance if this `SortedBidOrders` instance is non empty; `None` otherwise.
    */
  def headOption: Option[(OrderReferenceId, (OrderId, SingleUnitBid[T]))] = sortedOrders.headOption

  /** Tests whether this `SortedBidOrder` instance is empty.
    *
    * @return `true` is there is no `BidOrder` instance in this `SortedBidOrders` instance; `false` otherwise.
    */
  def isEmpty: Boolean = sortedOrders.isEmpty

  def splitOffTopOrder: (SortedSingleUnitBids[T], Option[(OrderReferenceId, (OrderId, SingleUnitBid[T]))]) = {
    headOption match {
      case Some((orderRefId, (_, askOrder))) =>
        val remainingOrders = orders - orderRefId
        val remainingUnits = numberUnits - askOrder.quantity
        (new SortedSingleUnitBids(remainingOrders, sortedOrders.tail, remainingUnits), headOption)
      case None =>
        (this, None)
    }
  }

  def tail: SortedSingleUnitBids[T] = {
    val (orderRefId, (_, order)) = sortedOrders.head
    val remainingUnits = numberUnits - order.quantity
    new SortedSingleUnitBids(orders - orderRefId, sortedOrders.tail, remainingUnits)
  }

}


/** Companion object for `SortedBidOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedSingleUnitBids {

  /** Create an instance of `SortedBidOrders` that does not contain any `BidOrder` instances.
    *
    * @param bidOrdering the ordering used to sort the underlying heap of `NewSingleUnitBid` instances.
    * @tparam T all `NewSingleUnitBid` instances stored in the heap should be for the same type of `Tradable`.
    * @return an instance of `SortedSingleUnitBids`.
    */
  def empty[T <: Tradable](bidOrdering: Ordering[SingleUnitBid[T]]): SortedSingleUnitBids[T] = {
    val orders = immutable.HashMap.empty[OrderReferenceId, (OrderId, SingleUnitBid[T])]
    val ordering = Ordering.by[(OrderReferenceId, (OrderId, SingleUnitBid[T])), SingleUnitBid[T]]{ case (_, (_, bid)) => bid }(bidOrdering)
    val sortedOrders = immutable.TreeSet.empty[(OrderReferenceId, (OrderId, SingleUnitBid[T]))](ordering)
    new SortedSingleUnitBids(orders, sortedOrders, Quantity.zero)
  }

}
