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
package org.economicsl.auctions.singleunit.orderbooks

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.singleunit.LimitAskOrder

import scala.collection.immutable


class SortedAskOrders[T <: Tradable] private(orders: immutable.TreeSet[LimitAskOrder[T]], val numberUnits: Quantity) {

  def + (order: LimitAskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders + order, numberUnits + order.quantity)
  }

  def - (order: LimitAskOrder[T]): SortedAskOrders[T] = {
    new SortedAskOrders(orders - order, numberUnits - order.quantity)
  }

  def contains(order: LimitAskOrder[T]): Boolean = orders.contains(order)

  def head: LimitAskOrder[T] = orders.head

  val headOption: Option[LimitAskOrder[T]] = orders.headOption

  val isEmpty: Boolean = orders.isEmpty

  val ordering: Ordering[LimitAskOrder[T]] = orders.ordering

  def tail: SortedAskOrders[T] = new SortedAskOrders(orders.tail, numberUnits - head.quantity)

}

object SortedAskOrders {

  def empty[T <: Tradable](ordering: Ordering[LimitAskOrder[T]]): SortedAskOrders[T] = {
    new SortedAskOrders(immutable.TreeSet.empty[LimitAskOrder[T]](ordering), Quantity(0))
  }

}
