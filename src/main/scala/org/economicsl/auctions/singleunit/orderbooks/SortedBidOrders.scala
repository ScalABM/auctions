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

import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.immutable


/** Class for storing a set of single-unit `BidOrder` instances in sorted order.
  *
  * @param orders
  * @param numberUnits
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
class SortedBidOrders[T <: Tradable] private(orders: immutable.TreeSet[BidOrder[T]], val numberUnits: Quantity) {

  def + (order: BidOrder[T]): SortedBidOrders[T] = {
    new SortedBidOrders(orders + order, numberUnits + order.quantity)
  }

  def - (order: BidOrder[T]): SortedBidOrders[T] = {
    new SortedBidOrders(orders - order, numberUnits - order.quantity)
  }

  def contains(order: BidOrder[T]): Boolean = orders.contains(order)

  def  head: BidOrder[T] = orders.head

  val headOption: Option[BidOrder[T]] = orders.headOption

  val isEmpty: Boolean = orders.isEmpty

  val ordering: Ordering[BidOrder[T]] = orders.ordering

}


/** Companion object for `SortedBidOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SortedBidOrders {

  def empty[T <: Tradable](ordering: Ordering[BidOrder[T]]): SortedBidOrders[T] = {
    new SortedBidOrders(immutable.TreeSet.empty[BidOrder[T]](ordering), Quantity(0))
  }

}
