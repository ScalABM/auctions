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

import org.economicsl.auctions.multiunit.orders.{AskOrder, Order}
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.{GenIterable, immutable}


/**
  *
  * @param existing sorted mapping from keys to collections of orders sharing a particular key.
  * @param numberUnits
  * @tparam K
  * @tparam O
  */
class OrderBook[K, O <: Order[_ <:Tradable]] private(existing: immutable.TreeMap[K, immutable.Queue[O]], val numberUnits: Quantity) {

  val ordering: Ordering[K] = existing.ordering

  /** Return a new `OrderBook` instance containing an additional `AskOrder` instance.
    *
    * @param key
    * @param order
    * @return
    * @note if this `OrderBook` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key as the new `AskOrder` instance, the new `AskOrder` instance is added to the existing collection.
    */
  def + (key: K, order: O): OrderBook[K, O] = {
    val current = existing.getOrElse(key, empty)
    new OrderBook(existing + (key -> current.enqueue(order)), numberUnits + order.quantity)
  }

  /** Return a new `OrderBook` instance containing an collection of `AskOrder` instances.
    *
    * @param key
    * @param orders
    * @return
    * @note if this `OrderBook` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key, then the new collection replaces the existing collection.
    */
  def + (key: K, orders: immutable.Queue[O]): OrderBook[K, O] = {
    val currentUnits = aggregate(existing.getOrElse(key, empty))
    val incomingUnits = aggregate(orders)
    val change = currentUnits - incomingUnits
    new OrderBook(existing + (key -> orders), numberUnits + change)
  }

  def - (key: K): OrderBook[K, O] = {
    existing.get(key) match {
      case Some(orders) =>
        val removedUnits = orders.foldLeft(Quantity(0))((total, order) => total + order.quantity)
        new OrderBook(existing - key, numberUnits - removedUnits)
      case None => this
    }
  }

  def - (key: K, order: O): OrderBook[K, O] = {
    existing.get(key) match {
      case Some(orders) =>
        val residualOrders = orders.diff(immutable.Queue(order))  // multi-set diff!
        if (residualOrders.isEmpty) {
          new OrderBook(existing - key, numberUnits - order.quantity)
        } else {
          new OrderBook(existing + (key -> residualOrders), numberUnits - order.quantity)
        }
      case None => this
    }
  }

  def contains(key: K): Boolean = existing.contains(key)

  def foldLeft[B](z: B)(op: (B, (K, immutable.Queue[O])) => B): B = {
    existing.foldLeft(z)(op)
  }

  def getOrElse(key: K, default: => immutable.Queue[O]): immutable.Queue[O] = {
    existing.getOrElse(key, default)
  }

  def headOption: Option[(K, O)] = {
    existing.headOption.flatMap{ case (key, orders) => orders.headOption.map(order => (key, order)) }
  }

  def isEmpty: Boolean = existing.isEmpty

  def mergeWith(other: OrderBook[K, O]): OrderBook[K, O] = {
    other.foldLeft(this) { case (ob, (key, orders)) =>
      val currentOrders = ob.getOrElse(key, empty)
      ob + (key, currentOrders.enqueue(orders))
    }
  }

  def nonEmpty: Boolean = existing.nonEmpty

  /* Use of GenIterable allows for use on both standard and parallel collections. */
  private[this] def aggregate(orders: GenIterable[O]): Quantity = {
    orders.aggregate(Quantity(0))((total, order) => total + order.quantity, _ + _ )
  }

  private[this] def empty: immutable.Queue[O] = immutable.Queue.empty[O]

}


object OrderBook {

  def empty[K, O <: Order[_ <: Tradable]](implicit ordering: Ordering[K]): OrderBook[K, O] = {
    new OrderBook(immutable.TreeMap.empty[K, immutable.Queue[O]](ordering), Quantity(0))
  }

}

