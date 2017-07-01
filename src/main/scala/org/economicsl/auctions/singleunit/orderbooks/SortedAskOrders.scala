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
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.core.{Quantity, Tradable}

import scala.collection.immutable


/** Class that stores a set of single-unit `AskOrder` instances in sorted order.
  *
  * @param existing
  * @param sorted a heap of single-unit `AskOrder` instances.
  * @param numberUnits the total number of `Tradable` units supplied by the issuers of the `AskOrder` instances
  *                    contained in this `SortedAskOrders` instance. Since this `SortedAskOrders` instance contains only
  *                    single-unit `AskOrder` instances, the `numberUnits` is also equal to the total number of
  *                    `AskOrder` instances.
  * @tparam T all `AskOrder` instances stored in the heap should be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class SortedAskOrders[T <: Tradable] private(existing: Map[Reference, (Token, AskOrder[T])],
                                                   sorted: immutable.TreeSet[(Reference, (Token, AskOrder[T]))],
                                                   val numberUnits: Quantity) {

  /** The ordering used to sort the `AskOrder` instances contained in this `SortedAskOrders` instance. */
  val ordering: Ordering[(Reference, (Token, AskOrder[T]))] = sorted.ordering

  /** Create a new `SortedAskOrders` instance containing the additional `AskOrder`.
    *
    * @param kv mapping between a unique identifier and an `AskOrder` that should be added.
    * @return a new `SortedAskOrder` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (Reference, (Token, AskOrder[T]))): SortedAskOrders[T] = {
    new SortedAskOrders(existing + kv, sorted + kv, numberUnits + kv._2._2.quantity)
  }

  /** Create a new `SortedAskOrders` instance with the given `AskOrder` removed from this `SortedAskOrders` instance.
    *
    * @param reference
    * @return
    */
  def - (reference: Reference): SortedAskOrders[T] = {
    existing.get(reference) match {
      case Some((token, order)) =>
        new SortedAskOrders(existing - reference, sorted - (reference -> (token -> order)), numberUnits - order.quantity)
      case None =>  // attempt to remove ask order that had already been processed!
        this
    }

  }

  /** Tests whether some `AskOrder` instance is contained in this `SortedAskOrders` instance.
    *
    * @param reference
    * @return `true` if the `order` is contained in this `SortedAskOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = existing.contains(reference)

  /** Selects the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    *
    * @return the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    */
  def head: (Reference, (Token, AskOrder[T])) = sorted.head

  /** Optionally selects the first `AskOrder` instance contained in this `SortedAskOrders` instance.
    *
    * @return Some `AskOrder` instance if this `SortedAskOrders` instance is non empty; `None` otherwise.
    */
  def headOption: Option[(Reference, (Token, AskOrder[T]))] = sorted.headOption

  /** Tests whether this `SortedAskOrder` instance is empty.
    *
    * @return `true` is there is no `AskOrder` instance in this `SortedAskOrders` instance; `false` otherwise.
    */
  def isEmpty: Boolean = sorted.isEmpty

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
    val existing = immutable.HashMap.empty[Reference, (Token, AskOrder[T])]
    val sorted = immutable.TreeSet.empty[(Reference, (Token, AskOrder[T]))](???)  // todo need to convert ordering!
    new SortedAskOrders(existing, sorted, Quantity.zero)
  }

}
