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
import org.economicsl.auctions.multiunit.LimitBidOrder

import scala.collection.immutable.TreeSet


private[orderbooks] class SortedBidOrders[T <: Tradable] private(existing: Map[UUID, LimitBidOrder[T]],
                                                                 sorted: TreeSet[LimitBidOrder[T]],
                                                                 val numberUnits: Quantity) {

  def - (uuid: UUID): SortedBidOrders[T] = existing.get(uuid) match {
    case Some(order) =>
      val remaining = Quantity(numberUnits.value - order.quantity.value)
      new SortedBidOrders(existing - uuid, sorted - order, remaining)
    case None => this
  }

  def contains(uuid: UUID): Boolean = existing.contains(uuid)

  def head: LimitBidOrder[T] = sorted.head

  val headOption: Option[LimitBidOrder[T]] = sorted.headOption

  val isEmpty: Boolean = existing.isEmpty && sorted.isEmpty

  val ordering: Ordering[LimitBidOrder[T]] = sorted.ordering

  def tail: SortedBidOrders[T] = {
    val remainingQuantity = Quantity(numberUnits.value - head.quantity.value)
    new SortedBidOrders(existing.tail, sorted.tail, remainingQuantity)
  }

  def updated(uuid: UUID, order: LimitBidOrder[T]): SortedBidOrders[T] = {
    val additional = Quantity(numberUnits.value + order.quantity.value)
    new SortedBidOrders(existing.updated(uuid, order), sorted + order, additional)
  }

}


object SortedBidOrders {

  def empty[T <: Tradable](ordering: Ordering[LimitBidOrder[T]]): SortedBidOrders[T] = {
    new SortedBidOrders(Map.empty[UUID, LimitBidOrder[T]], TreeSet.empty[LimitBidOrder[T]](ordering), Quantity(0))
  }

}
