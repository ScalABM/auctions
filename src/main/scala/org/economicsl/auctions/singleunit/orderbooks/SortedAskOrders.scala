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

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.singleunit.AskOrder

import scala.collection.immutable


/** Class for storing a set of `AskOrder` instances in sorted order.
  *
  * @param orders
  * @param numberUnits
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
class SortedAskOrders[T <: Tradable] private(orders: immutable.TreeSet[AskOrder[T]], val numberUnits: Quantity) {

  def + (order: AskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders + order, numberUnits + order.quantity)
  }

  def - (order: AskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders - order, numberUnits - order.quantity)
  }

  def contains(order: AskOrder[T]): Boolean = orders.contains(order)

  def head: AskOrder[T] = orders.head

  val headOption: Option[AskOrder[T]] = orders.headOption

  val isEmpty: Boolean = orders.isEmpty

  val ordering: Ordering[AskOrder[T]] = orders.ordering

  def tail: SortedAskOrders[T] = new SortedAskOrders(orders.tail, numberUnits - head.quantity)

}


/** Companion object for `SortedAskOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedAskOrders {

  def empty[T <: Tradable](ordering: Ordering[AskOrder[T]]): SortedAskOrders[T] = {
    new SortedAskOrders(immutable.TreeSet.empty[AskOrder[T]](ordering), Quantity(0))
  }

}
