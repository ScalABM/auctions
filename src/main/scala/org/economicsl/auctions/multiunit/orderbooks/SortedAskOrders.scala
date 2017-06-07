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

import org.economicsl.auctions.multiunit.orders.AskOrder
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.immutable


/**
  *
  * @param existing sorted mapping from keys to collections of orders sharing a particular key.
  * @param numberUnits
  * @tparam K
  * @tparam T
  */
class SortedAskOrders[K, T <: Tradable] private(existing: immutable.TreeMap[K, immutable.Queue[AskOrder[T]]], val numberUnits: Quantity) {

  val ordering: Ordering[K] = existing.ordering

  /** Return a new `SortedAskOrders` instance containing an additional `AskOrder` instance.
    *
    * @param key
    * @param order
    * @return
    * @note if this `SortedAskOrders` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key as the new `AskOrder` instance, the new `AskOrder` instance is added to the existing collection.
    */
  def + (key: K, order: AskOrder[T]): SortedAskOrders[K, T] = {
    val current = existing.getOrElse(key, empty)
    new SortedAskOrders(existing + (key -> current.enqueue(order)), numberUnits + order.quantity)
  }

  /** Return a new `SortedAskOrders` instance containing an collection of `AskOrder` instances.
    *
    * @param key
    * @param orders
    * @return
    * @note if this `SortedAskOrders` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key, then the new collection replaces the existing collection.
    */
  def + (key: K, orders: immutable.Queue[AskOrder[T]]): SortedAskOrders[K, T] = {
    val currentUnits = aggregate(existing.getOrElse(key, empty))
    val incomingUnits = aggregate(orders)
    val change = currentUnits - incomingUnits
    new SortedAskOrders(existing + (key -> orders), numberUnits + change)
  }

  def - (key: K): SortedAskOrders[K, T] = {
    existing.get(key) match {
      case Some(orders) =>
        val removedUnits = orders.foldLeft(Quantity(0))((total, order) => total + order.quantity)
        new SortedAskOrders(existing - key, numberUnits - removedUnits)
      case None => this
    }
  }

  def - (key: K, order: AskOrder[T]): SortedAskOrders[K, T] = {
    existing.get(key) match {
      case Some(orders) =>
        val residualOrders = orders.diff(immutable.Queue(order))  // multi-set diff!
        if (residualOrders.isEmpty) {
          new SortedAskOrders(existing - key, numberUnits - order.quantity)
        } else {
          new SortedAskOrders(existing + (key -> residualOrders), numberUnits - order.quantity)
        }
      case None => this
    }
  }

  def contains(key: K): Boolean = existing.contains(key)

  def foldLeft[B](z: B)(op: (B, (K, immutable.Queue[AskOrder[T]])) => B): B = {
    existing.foldLeft(z)(op)
  }

  def getOrElse(key: K, default: => immutable.Queue[AskOrder[T]]): immutable.Queue[AskOrder[T]] = {
    existing.getOrElse(key, default)
  }

  def headOption: Option[(K, AskOrder[T])] = {
    existing.headOption.flatMap{ case (key, orders) => orders.headOption.map(order => (key, order)) }
  }

  def isEmpty: Boolean = existing.isEmpty

  def mergeWith(other: SortedAskOrders[K, T]): SortedAskOrders[K, T] = {
    other.foldLeft(this) { case (ob, (key, orders)) =>
      val currentOrders = ob.getOrElse(key, empty)
      ob + (key, currentOrders.enqueue(orders))
    }
  }

  def nonEmpty: Boolean = existing.nonEmpty

  def splitAt(quantity: Quantity): (SortedAskOrders[K, T], SortedAskOrders[K, T]) = {

    @annotation.tailrec
    def loop(prefix: SortedAskOrders[K, T], suffix: SortedAskOrders[K, T]): (SortedAskOrders[K, T], SortedAskOrders[K, T]) = {
      suffix.headOption match {
        case Some((key, askOrder)) =>
          val remainingQuantity = quantity - prefix.numberUnits
          if (remainingQuantity > askOrder.quantity) {
            loop(prefix + (key, askOrder), suffix - (key, askOrder))
          } else if (remainingQuantity < askOrder.quantity) {
            (prefix + (key, askOrder.withQuantity(remainingQuantity)), suffix - (key, askOrder) + (key, askOrder.withQuantity(askOrder.quantity - remainingQuantity)))
          } else {
            (prefix + (key, askOrder), suffix - (key, askOrder))
          }
        case None =>
          (prefix, SortedAskOrders.empty(ordering))
      }
    }
    loop(SortedAskOrders.empty[K, T](ordering), this)

  }

  private[this] def aggregate(orders: immutable.Queue[AskOrder[T]]): Quantity = {
    orders.aggregate(Quantity(0))((total, order) => total + order.quantity, _ + _ )
  }

  private[this] def empty: immutable.Queue[AskOrder[T]] = immutable.Queue.empty[AskOrder[T]]

}


object SortedAskOrders {

  def empty[K, T <: Tradable](implicit ordering: Ordering[K]): SortedAskOrders[K, T] = {
    new SortedAskOrders(immutable.TreeMap.empty[K, immutable.Queue[AskOrder[T]]](ordering), Quantity(0))
  }

}

