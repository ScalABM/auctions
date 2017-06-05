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

import org.economicsl.auctions.multiunit.orders.BidOrder
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.immutable.TreeSet


private[orderbooks] class SortedBidOrders[T <: Tradable] private(existing: Map[UUID, BidOrder[T]],
                                                                 val sorted: TreeSet[(UUID, BidOrder[T])],
                                                                 val numberUnits: Quantity) {

  assert(existing.size == sorted.size)

  def apply(uuid: UUID): BidOrder[T] = existing(uuid)

  def + (kv: (UUID, BidOrder[T])): SortedBidOrders[T] = {
    new SortedBidOrders(existing + kv, sorted + kv, numberUnits + kv._2.quantity)
  }

  def - (uuid: UUID): SortedBidOrders[T] = existing.get(uuid) match {
    case Some(order) =>
      val remaining = Quantity(numberUnits.value - order.quantity.value)
      new SortedBidOrders(existing - uuid, sorted - ((uuid, order)), remaining)
    case None => this
  }

  def contains(uuid: UUID): Boolean = existing.contains(uuid)

  def head: (UUID, BidOrder[T]) = sorted.head

  val headOption: Option[(UUID, BidOrder[T])] = sorted.headOption

  val isEmpty: Boolean = existing.isEmpty && sorted.isEmpty

  val nonEmpty: Boolean = existing.nonEmpty && sorted.nonEmpty

  val ordering: Ordering[(UUID, BidOrder[T])] = sorted.ordering

  val size: Int = existing.size

  def mergeWith(other: SortedBidOrders[T]): SortedBidOrders[T] = {
    ???
  }

  def splitAt(quantity: Quantity): (SortedBidOrders[T], SortedBidOrders[T]) = {

    def split(order: BidOrder[T], quantity: Quantity): (BidOrder[T], BidOrder[T]) = {
      val residual = order.quantity - quantity
      (order.withQuantity(quantity), order.withQuantity(residual))
    }

    @annotation.tailrec
    def loop(in: SortedBidOrders[T], out: SortedBidOrders[T]): (SortedBidOrders[T], SortedBidOrders[T]) = {
      val unMatched = quantity - in.numberUnits
      val (uuid, bidOrder) = out.head
      if (unMatched > bidOrder.quantity) {
        loop(in + (uuid -> bidOrder), out - uuid)
      } else if (unMatched < bidOrder.quantity) {
        val (matched, residual) = split(bidOrder, unMatched)
        (in + (uuid -> matched), out.updated(uuid, residual))
      } else {
        (in + (uuid -> bidOrder), out - uuid)
      }
    }
    loop(SortedBidOrders.empty[T](sorted.ordering), this)

  }

  def updated(uuid: UUID, order: BidOrder[T]): SortedBidOrders[T] = {
    val bidOrder = this(uuid)
    val change = order.quantity - bidOrder.quantity
    new SortedBidOrders(existing.updated(uuid, order), sorted - ((uuid, bidOrder)) + ((uuid, order)), numberUnits + change)
  }

}


object SortedBidOrders {

  def empty[T <: Tradable](implicit ordering: Ordering[(UUID, BidOrder[T])]): SortedBidOrders[T] = {
    new SortedBidOrders(Map.empty[UUID, BidOrder[T]], TreeSet.empty[(UUID, BidOrder[T])](ordering), Quantity(0))
  }

}
