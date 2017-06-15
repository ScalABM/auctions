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

import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.core.{Quantity, Tradable}

import scala.collection.immutable


/** Class that stores a set of single-unit `AskOrder` instances in sorted order.
  *
  * @param orders a heap of single-unit `AskOrder` instances.
  * @param numberUnits the total number of `Tradablle` units supplied by the issuers of the `AskOrder` instances
  *                    contained in this `SortedAskOrders` instance. Since this `SortedAskOrders` instance contains only
  *                    single-unit `AskOrder` instances, the `numberUnits` is also equal to the total number of
  *                    `AskOrder` instances.
  * @tparam T all `AskOrder` instances stored in the heap should be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class SortedAskOrders[T <: Tradable] private(orders: immutable.TreeSet[AskOrder[T]], val numberUnits: Quantity) {

  /** The ordering used to sort the `AskOrder` instances contained in this `SortedAskOrders` instance. */
  val ordering: Ordering[AskOrder[T]] = orders.ordering

  /** Create a new `SortedAskOrders` instance containing the additional `AskOrder`.
    *
    * @param order an `AskOrder` that should be added.
    * @return a new `SortedAskOrder` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (order: AskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders + order, numberUnits + order.quantity)
  }

  /** Create a new `SortedAskOrders` instance with the given `AskOrder` removed from this `SortedAskOrders` instance.
    *
    * @param order an `AskOrder` that should be removed.
    * @return a new `SortedAskOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def - (order: AskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders - order, numberUnits - order.quantity)
  }

  /** Tests whether some `AskOrder` instance is contained in this `SortedAskOrders` instance.
    *
    * @param order the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `SortedAskOrders` instance; `false` otherwise.
    */
  def contains(order: AskOrder[T]): Boolean = orders.contains(order)

  /** Selects the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    *
    * @return the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    */
  def  head: AskOrder[T] = orders.head

  /** Optionally selects the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    *
    * @return Some `AskOrder` instance if this `SortedAskOrders` instance is non empty; `None` otherwise.
    */
  def headOption: Option[AskOrder[T]] = orders.headOption

  /** Tests whether this `SortedAskOrder` instance is empty.
    *
    * @return `true` is there is no `AskOrder` instance in this `SortedAskOrders` instance; `false` otherwise.
    */
  def isEmpty: Boolean = orders.isEmpty

}


/** Companion object for `SortedAskOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedAskOrders {

  /** Create an instance of `SortedAskOrders` that does not contain any `AskOrder` instances.
    *
    * @param ordering the ordering used to sort the underlying heap of `AskOrder` instances.
    * @tparam T all `AskOrder` instances stored in the heap should be for the same type of `Tradable`.
    * @return an instance of `SortedAskOrders`.
    */
  def empty[T <: Tradable](ordering: Ordering[AskOrder[T]]): SortedAskOrders[T] = {
    new SortedAskOrders(immutable.TreeSet.empty[AskOrder[T]](ordering), Quantity(0))
  }

}
