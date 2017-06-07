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

import org.economicsl.auctions.multiunit.orders.BidOrder
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.immutable


class SortedBidOrders[K, T <: Tradable] private(existing: immutable.TreeMap[K, immutable.Queue[BidOrder[T]]], val numberUnits: Quantity) {

  val ordering: Ordering[K] = existing.ordering

  /** Return a new `SortedBidOrders` instance containing an additional `BidOrder` instance.
    *
    * @param key
    * @param order
    * @return
    * @note if this `SortedBidOrders` instance contains an existing collection of `BidOrder` instances sharing the same
    *       key as the new `BidOrder` instance, the new `BidOrder` instance is added to the existing collection.
    */
  def + (key: K, order: BidOrder[T]): SortedBidOrders[K, T] = {
    val current = existing.getOrElse(key, immutable.Queue.empty[BidOrder[T]])
    new SortedBidOrders(existing + (key -> current.enqueue(order)), numberUnits + order.quantity)
  }

  /** Return a new `SortedBidOrders` instance containing an collection of `BidOrder` instances.
    *
    * @param key
    * @param orders
    * @return
    * @note if this `SortedBidOrders` instance contains an existing collection of `BidOrder` instances sharing the same
    *       key, then the new collection replaces the existing collection.
    */
  def + (key: K, orders: immutable.Queue[BidOrder[T]]): SortedBidOrders[K, T] = {
    val currentOrders = existing.getOrElse(key, immutable.Queue.empty[BidOrder[T]])
    val currentUnits = currentOrders.foldLeft(Quantity(0)){ case (total, order) => total + order.quantity }
    val incomingUnits = orders.foldLeft(Quantity(0)){ case (total, order) => total + order.quantity }
    val netChange: Quantity = currentUnits - incomingUnits
    new SortedBidOrders(existing + (key -> orders), numberUnits + netChange)
  }

  def - (key: K): SortedBidOrders[K, T] = {
    existing.get(key) match {
      case Some(orders) =>
        val removedUnits = orders.foldLeft(Quantity(0))((total, order) => total + order.quantity)
        new SortedBidOrders(existing - key, numberUnits - removedUnits)
      case None => this
    }
  }

  def - (key: K, order: BidOrder[T]): SortedBidOrders[K, T] = {
    existing.get(key) match {
      case Some(orders) =>
        val residualOrders = orders.diff(immutable.Queue(order))  // multi-set diff available on SeqLike collections!
        if (residualOrders.isEmpty) {
          new SortedBidOrders(existing - key, numberUnits - order.quantity)
        } else {
          new SortedBidOrders(existing + (key -> residualOrders), numberUnits - order.quantity)
        }
      case None => this
    }
  }

  def contains(key: K): Boolean = existing.contains(key)

  def foldLeft[B](z: B)(op: (B, (K, immutable.Queue[BidOrder[T]])) => B): B = {
    existing.foldLeft(z)(op)
  }

  def getOrElse(key: K, default: => immutable.Queue[BidOrder[T]]): immutable.Queue[BidOrder[T]] = {
    existing.getOrElse(key, default)
  }

  def headOption: Option[(K, BidOrder[T])] = {
    existing.headOption.flatMap{ case (key, orders) => orders.headOption.map(order => (key, order)) }
  }

  def isEmpty: Boolean = existing.isEmpty

  def mergeWith(other: SortedBidOrders[K, T]): SortedBidOrders[K, T] = {
    other.foldLeft(this) { case (ob, (key, orders)) =>
      val currentOrders = ob.getOrElse(key, immutable.Queue.empty[BidOrder[T]])
      ob + (key, currentOrders.enqueue(orders))
    }
  }

  def nonEmpty: Boolean = existing.nonEmpty

  def splitAt(quantity: Quantity): (SortedBidOrders[K, T], SortedBidOrders[K, T]) = {

    @annotation.tailrec
    def loop(prefix: SortedBidOrders[K, T], suffix: SortedBidOrders[K, T]): (SortedBidOrders[K, T], SortedBidOrders[K, T]) = {
      suffix.headOption match {
        case Some((key, bidOrder)) =>
          val remainingQuantity = quantity - prefix.numberUnits
          if (remainingQuantity > bidOrder.quantity) {
            loop(prefix + (key, bidOrder), suffix - (key, bidOrder))
          } else if (remainingQuantity < bidOrder.quantity) {
            (prefix + (key, bidOrder.withQuantity(remainingQuantity)), suffix - (key, bidOrder) + (key, bidOrder.withQuantity(bidOrder.quantity - remainingQuantity)))
          } else {
            (prefix + (key, bidOrder), suffix - (key, bidOrder))
          }
        case None =>
          (prefix, SortedBidOrders.empty[K, T](ordering))
      }
    }
    loop(SortedBidOrders.empty[K, T](ordering), this)

  }

}


object SortedBidOrders {

  def empty[K, T <: Tradable](implicit ordering: Ordering[K]): SortedBidOrders[K, T] = {
    new SortedBidOrders(immutable.TreeMap.empty[K, immutable.Queue[BidOrder[T]]](ordering), Quantity(0))
  }

}
