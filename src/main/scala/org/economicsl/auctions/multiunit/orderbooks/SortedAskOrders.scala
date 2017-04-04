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
                                                                 sorted: TreeSet[(UUID, LimitAskOrder[T])],
                                                                 val quantity: Quantity) {
  assert(existing.size == sorted.size)

  def apply(uuid: UUID): LimitAskOrder[T] = existing(uuid)

  def + (kv: (UUID, LimitAskOrder[T])): SortedAskOrders[T] = {
    new SortedAskOrders(existing + kv, sorted + kv, quantity + kv._2.quantity)
  }

  def - (uuid: UUID): SortedAskOrders[T] = existing.get(uuid) match {
    case Some(order) =>
      val remaining = Quantity(quantity.value - order.quantity.value)
      new SortedAskOrders(existing - uuid, sorted - ((uuid, order)), remaining)
    case None => this
  }

  def contains(uuid: UUID): Boolean = existing.contains(uuid)

  def head: (UUID, LimitAskOrder[T]) = sorted.head

  val headOption: Option[(UUID, LimitAskOrder[T])] = sorted.headOption

  val isEmpty: Boolean = existing.isEmpty && sorted.isEmpty

  val nonEmpty: Boolean = existing.nonEmpty && sorted.nonEmpty

  val ordering: Ordering[(UUID, LimitAskOrder[T])] = sorted.ordering

  val size: Int = existing.size

  def mergeWith(other: SortedAskOrders[T]): SortedAskOrders[T] = {
    ???
  }

  def splitAt(quantity: Quantity): (SortedAskOrders[T], SortedAskOrders[T]) = {

    def split(order: LimitAskOrder[T], quantity: Quantity): (LimitAskOrder[T], LimitAskOrder[T]) = {
      val residual = order.quantity - quantity
      (order.withQuantity(quantity), order.withQuantity(residual))
    }

    @annotation.tailrec
    def loop(in: SortedAskOrders[T], out: SortedAskOrders[T]): (SortedAskOrders[T], SortedAskOrders[T]) = {
      val unMatched = quantity - in.quantity
      val (uuid, askOrder) = out.head
      if (unMatched > askOrder.quantity) {
        loop(in + (uuid -> askOrder), out - uuid)
      } else if (unMatched < askOrder.quantity) {
        val (matched, residual) = split(askOrder, unMatched)
        (in + (uuid -> matched), out.update(uuid, residual))
      } else {
        (in + (uuid -> askOrder), out - uuid)
      }
    }
    loop(SortedAskOrders.empty[T](sorted.ordering), this)

  }

  def update(uuid: UUID, order: LimitAskOrder[T]): SortedAskOrders[T] = {
    val askOrder = this(uuid)
    val change = order.quantity - askOrder.quantity
    new SortedAskOrders(existing.updated(uuid, order), sorted - ((uuid, askOrder)) + ((uuid, order)), quantity + change)
  }

}


object SortedAskOrders {

  def empty[T <: Tradable](implicit ordering: Ordering[(UUID, LimitAskOrder[T])]): SortedAskOrders[T] = {
    new SortedAskOrders(Map.empty[UUID, LimitAskOrder[T]], TreeSet.empty[(UUID, LimitAskOrder[T])](ordering), Quantity(0))
  }

}

