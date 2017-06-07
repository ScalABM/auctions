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

import org.economicsl.auctions.multiunit.orders.Order
import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenIterable, immutable}


/**
  *
  * @param existing sorted mapping from keys to collections of orders sharing a particular key.
  * @param numberUnits
  * @tparam K
  * @tparam O
  */
class OrderBook[K, O <: Order[_ <:Tradable], CC <: GenIterable[O]] private(existing: immutable.TreeMap[K, CC],
                                                                           val numberUnits: Quantity)
                                                                          (implicit cbf: CanBuildFrom[CC, O, CC]) {

  val ordering: Ordering[K] = existing.ordering

  /** Return a new `OrderBook` instance containing an additional `AskOrder` instance.
    *
    * @param key
    * @param order
    * @return
    * @note if this `OrderBook` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key as the new `AskOrder` instance, the new `AskOrder` instance is added to the existing collection.
    */
  def + (key: K, order: O): OrderBook[K, O, CC] = {
    val current = existing.getOrElse(key, empty)
    val builder = cbf(current)
    builder += order
    new OrderBook(existing + (key -> builder.result()), numberUnits + order.quantity)
  }

  /** Return a new `OrderBook` instance containing an collection of `AskOrder` instances.
    *
    * @param key
    * @param orders
    * @return
    * @note if this `OrderBook` instance contains an existing collection of `AskOrder` instances sharing the same
    *       key, then the new collection replaces the existing collection.
    */
  def + (key: K, orders: CC): OrderBook[K, O, CC] = {
    val currentUnits = aggregate(existing.getOrElse(key, empty))
    val incomingUnits = aggregate(orders)
    val change = currentUnits - incomingUnits
    new OrderBook(existing + (key -> orders), numberUnits + change)
  }

  def - (key: K): OrderBook[K, O, CC] = {
    existing.get(key) match {
      case Some(orders) =>
        val removedUnits = orders.foldLeft(Quantity(0))((total, order) => total + order.quantity)
        new OrderBook(existing - key, numberUnits - removedUnits)
      case None => this
    }
  }

  def - (key: K, order: O): OrderBook[K, O, CC] = {
    existing.get(key) match {
      case Some(orders) =>
        val builder = cbf()
        orders.foreach(o => if (o != order) builder += o)
        val residualOrders = builder.result()
        if (residualOrders.isEmpty) {
          new OrderBook(existing - key, numberUnits - order.quantity)
        } else {
          new OrderBook(existing + (key -> residualOrders), numberUnits - order.quantity)
        }
      case None => this
    }
  }

  def contains(key: K): Boolean = existing.contains(key)

  def foldLeft[B](z: B)(op: (B, (K, CC)) => B): B = {
    existing.foldLeft(z)(op)
  }

  def getOrElse(key: K, default: => CC): CC = {
    existing.getOrElse(key, default)
  }

  def headOption: Option[(K, O)] = {
    existing.headOption.flatMap{ case (key, orders) => orders.headOption.map(order => (key, order)) }
  }

  def isEmpty: Boolean = existing.isEmpty

  def mergeWith(other: OrderBook[K, O, CC]): OrderBook[K, O, CC] = {
    other.foldLeft(this) { case (ob, (key, orders)) =>
      val current = ob.getOrElse(key, empty)
      val builder = cbf(current)
      orders.foreach(order => builder += order)
      ob + (key, builder.result())
    }
  }

  def nonEmpty: Boolean = existing.nonEmpty

  /* Use of GenIterable allows for use on both standard and parallel collections. */
  private[this] def aggregate(orders: GenIterable[O]): Quantity = {
    orders.aggregate(Quantity(0))((total, order) => total + order.quantity, _ + _ )
  }

  private[this] def empty: CC = {
    cbf().result()
  }

}


object OrderBook {

  def empty[K, O <: Order[_ <: Tradable], CC <: GenIterable[O]](implicit ordering: Ordering[K], cbf: CanBuildFrom[CC, O, CC]): OrderBook[K, O, CC] = {
    val initial = immutable.TreeMap.empty(ordering)
    new OrderBook[K, O, CC](initial, Quantity(0))(cbf)
  }

}