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
import org.economicsl.auctions.messages.{OrderReferenceId, SingleUnitOffer}
import org.economicsl.core.{Quantity, Tradable}

import scala.collection.immutable


/** Class that stores a set of single-unit `SingleUnitOffer` instances in sorted order.
  *
  * @param orders
  * @param sortedOrders a heap of single-unit `SingleUnitOffer` instances.
  * @param numberUnits the total number of `Tradable` units supplied by the issuers of the `SingleUnitOffer` instances
  *                    contained in this `SortedSingleUnitOffers` instance. Since this `SortedSingleUnitOffers` instance contains only
  *                    single-unit `SingleUnitOffer` instances, the `numberUnits` is also equal to the total number of
  *                    `SingleUnitOffer` instances.
  * @tparam T all `SingleUnitOffer` instances stored in the heap should be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class SortedSingleUnitOffers[T <: Tradable] private(
  orders: Map[OrderReferenceId, SingleUnitOffer[T]],
  sortedOrders: immutable.TreeSet[(OrderReferenceId, SingleUnitOffer[T])],
  val numberUnits: Quantity)
    extends OrderBook[T, SingleUnitOffer[T], SortedSingleUnitOffers[T]] {

  /** The ordering used to sort the `SingleUnitOffer` instances contained in this `SortedSingleUnitOffers` instance. */
  val ordering: Ordering[(OrderReferenceId, SingleUnitOffer[T])] = sortedOrders.ordering

  /** Create a new `SortedSingleUnitOffers` instance containing the additional `SingleUnitOffer`.
    *
    * @param kv mapping between a unique identifier and an `SingleUnitOffer` that should be added.
    * @return a new `SortedAskOrder` instance that contains all of the `SingleUnitOffer` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (OrderReferenceId, SingleUnitOffer[T])): SortedSingleUnitOffers[T] = kv match {
    case (_, order) =>
      new SortedSingleUnitOffers(orders + kv, sortedOrders + kv, numberUnits + order.quantity)
  }

  /** Create a new `SortedSingleUnitOffers` instance with the given `SingleUnitOffer` removed from this `SortedSingleUnitOffers` instance.
    *
    * @param orderRefId
    * @return
    */
  def - (orderRefId: OrderReferenceId): (SortedSingleUnitOffers[T], Option[SingleUnitOffer[T]]) = {
    orders.get(orderRefId) match {
      case Some(order) =>
        val remainingOrders = orders - orderRefId
        val remainingSortedOrders = sortedOrders - (orderRefId -> order)
        val remainingUnits = numberUnits - order.quantity
        (new SortedSingleUnitOffers(remainingOrders, remainingSortedOrders, remainingUnits), Some(order))
      case None =>  // attempt to remove ask order that had already been processed!
        (this, None)
    }
  }

  /** Tests whether some `SingleUnitOffer` instance is contained in this `SortedSingleUnitOffers` instance.
    *
    * @param orderRefId
    * @return `true` if the `order` is contained in this `SortedSingleUnitOffers` instance; `false` otherwise.
    */
  def contains(orderRefId: OrderReferenceId): Boolean = orders.contains(orderRefId)

  
  def get(orderRefId: OrderReferenceId): Option[SingleUnitOffer[T]] = {
    orders.get(orderRefId)
  }

  /** Selects the first the first key-value mapping contained in this `SortedSingleUnitOffers` instance.
    *
    * @return the first the first key-value mapping contained in this `SortedSingleUnitOffers` instance.
    */
  def head: (OrderReferenceId, SingleUnitOffer[T]) = sortedOrders.head

  /** Optionally selects the first key-value mapping contained in this `SortedSingleUnitOffers` instance.
    *
    * @return Some key-value mapping if this `SortedSingleUnitOffers` instance is non empty; `None` otherwise.
    */
  def headOption: Option[(OrderReferenceId, SingleUnitOffer[T])] = sortedOrders.headOption

  /** Tests whether this `SortedAskOrder` instance is empty.
    *
    * @return `true` is there is no `SingleUnitOffer` instance in this `SortedSingleUnitOffers` instance; `false` otherwise.
    */
  def isEmpty: Boolean = sortedOrders.isEmpty

  def dequeue: (SortedSingleUnitOffers[T], Option[(OrderReferenceId, SingleUnitOffer[T])]) = {
    headOption match {
      case Some((orderRefId, offer)) =>
        val remainingOrders = orders - orderRefId
        val remainingUnits = numberUnits - offer.quantity
        (new SortedSingleUnitOffers(remainingOrders, sortedOrders.tail, remainingUnits), headOption)
      case None =>
        (this, None)
    }
  }

  def tail: SortedSingleUnitOffers[T] = {
    val (orderRefId, order) = sortedOrders.head
    val remainingUnits = numberUnits - order.quantity
    new SortedSingleUnitOffers(orders - orderRefId, sortedOrders.tail, remainingUnits)
  }

}


/** Companion object for `SortedSingleUnitOffers`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedSingleUnitOffers {

  /** Create an instance of `SortedSingleUnitOffers` that does not contain any `SingleUnitOffer` instances.
    *
    * @param ordering the ordering used to sort the underlying heap of `SingleUnitOffer` instances.
    * @tparam T all `SingleUnitOffer` instances stored in the heap should be for the same type of `Tradable`.
    * @return an instance of `SortedSingleUnitOffers`.
    */
  def empty[T <: Tradable](ordering: Ordering[SingleUnitOffer[T]]): SortedSingleUnitOffers[T] = {
    val existing = immutable.HashMap.empty[OrderReferenceId, SingleUnitOffer[T]]
    val byAskOrder = Ordering.by[(OrderReferenceId, SingleUnitOffer[T]), SingleUnitOffer[T]]{ case (_, order) => order }(ordering)
    val sorted = immutable.TreeSet.empty[(OrderReferenceId, SingleUnitOffer[T])](byAskOrder)
    new SortedSingleUnitOffers(existing, sorted, Quantity.zero)
  }

}
