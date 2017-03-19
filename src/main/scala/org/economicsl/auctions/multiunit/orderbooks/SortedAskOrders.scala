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

import java.util.UUID

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.multiunit.LimitAskOrder

import scala.collection.immutable.TreeSet


private[orderbooks] class SortedAskOrders[T <: Tradable] private(existing: Map[UUID, LimitAskOrder[T]],
                                                                 sorted: TreeSet[LimitAskOrder[T]],
                                                                 val numberUnits: Quantity) {

  def - (uuid: UUID): SortedAskOrders[T] = existing.get(uuid) match {
    case Some(order) =>
      val remaining = Quantity(numberUnits.value - order.quantity.value)
      new SortedAskOrders(existing - uuid, sorted - order, remaining)
    case None => this
  }

  def contains(uuid: UUID): Boolean = existing.contains(uuid)

  def head: LimitAskOrder[T] = sorted.head

  val headOption: Option[LimitAskOrder[T]] = sorted.headOption

  val isEmpty: Boolean = existing.isEmpty && sorted.isEmpty

  val ordering: Ordering[LimitAskOrder[T]] = sorted.ordering

  def tail: SortedAskOrders[T] = {
    val remainingQuantity = Quantity(numberUnits.value - head.quantity.value)
    new SortedAskOrders(existing.tail, sorted.tail, remainingQuantity)
  }

  def updated(uuid: UUID, order: LimitAskOrder[T]): SortedAskOrders[T] = {
    val additional = Quantity(numberUnits.value + order.quantity.value)
    new SortedAskOrders(existing.updated(uuid, order), sorted + order, additional)
  }

}

object SortedAskOrders {

  def empty[T <: Tradable](ordering: Ordering[LimitAskOrder[T]]): SortedAskOrders[T] = {
    new SortedAskOrders(Map.empty[UUID, LimitAskOrder[T]], TreeSet.empty[LimitAskOrder[T]](ordering), Quantity(0))
  }

}

