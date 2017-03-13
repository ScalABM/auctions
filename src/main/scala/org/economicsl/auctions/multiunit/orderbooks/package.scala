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
package org.economicsl.auctions.multiunit

import org.economicsl.auctions.{Order, Quantity, Tradable}

import scala.collection.{GenIterable, immutable}


/** Documentation for multi-unit orderbooks goes here! */
package object orderbooks {

  class SortedAskOrders[T <: Tradable] private(orders: immutable.TreeSet[LimitAskOrder[T]], val numberUnits: Quantity) {

    def + (order: LimitAskOrder[T]): SortedAskOrders[T] = {
      new SortedAskOrders(orders + order, Quantity(numberUnits.value + order.quantity.value))
    }

    def ++ (additional: TraversableOnce[LimitAskOrder[T]]): SortedAskOrders[T] = {
      new SortedAskOrders(orders ++ additional, Quantity(numberUnits.value + additional.reduce((a1, a2) => Quantity(a1.quantity.value + a2.quantity.value)).value))
    }

    def - (order: LimitAskOrder[T]): SortedAskOrders[T] = {
      new SortedAskOrders(orders - order, Quantity(numberUnits.value - order.quantity.value))
    }

    def -- (excess: TraversableOnce[LimitAskOrder[T]]): SortedAskOrders[T] = {
      new SortedAskOrders(orders -- excess, Quantity(numberUnits.value - excess.reduce((a1, a2) => Quantity(a1.quantity.value + a2.quantity.value)).value))
    }

    def contains(order: LimitAskOrder[T]): Boolean = orders.contains(order)

    def head: LimitAskOrder[T] = orders.head

    val headOption: Option[LimitAskOrder[T]] = orders.headOption

    val isEmpty: Boolean = orders.isEmpty

    val ordering: Ordering[LimitAskOrder[T]] = orders.ordering

    def tail: SortedAskOrders[T] = new SortedAskOrders(orders.tail, Quantity(numberUnits.value - head.quantity.value))

  }

  object SortedAskOrders {

    def empty[T <: Tradable](ordering: Ordering[LimitAskOrder[T]]): SortedAskOrders[T] = {
      new SortedAskOrders(immutable.TreeSet.empty[LimitAskOrder[T]](ordering), Quantity(0))
    }

  }


  class SortedBidOrders[T <: Tradable] private(orders: immutable.TreeSet[LimitBidOrder[T]], val numberUnits: Quantity) {

    def + (order: LimitBidOrder[T]): SortedBidOrders[T] = {
      new SortedBidOrders(orders + order, Quantity(numberUnits.value + order.quantity.value))
    }

    def ++ (additional: GenIterable[LimitBidOrder[T]]): SortedBidOrders[T] = {
      new SortedBidOrders(orders ++ additional, Quantity(numberUnits.value + totalQuantity(additional).value))
    }

    def - (order: LimitBidOrder[T]): SortedBidOrders[T] = {
      new SortedBidOrders(orders - order, Quantity(numberUnits.value - order.quantity.value))
    }

    def -- (excess: TraversableOnce[LimitBidOrder[T]]): SortedBidOrders[T] = {
      new SortedBidOrders(orders -- excess, Quantity(numberUnits.value - excess.reduce((b1, b2) => Quantity(b1.quantity.value + b2.quantity.value)).value))
    }

    def contains(order: LimitBidOrder[T]): Boolean = orders.contains(order)

    def head: LimitBidOrder[T] = orders.head

    val headOption: Option[LimitBidOrder[T]] = orders.headOption

    val isEmpty: Boolean = orders.isEmpty

    val ordering: Ordering[LimitBidOrder[T]] = orders.ordering

    def tail: SortedBidOrders[T] = new SortedBidOrders(orders.tail, Quantity(numberUnits.value - head.quantity.value))

  }

  object SortedBidOrders {

    def empty[T <: Tradable](ordering: Ordering[LimitBidOrder[T]]): SortedBidOrders[T] = {
      new SortedBidOrders(immutable.TreeSet.empty[LimitBidOrder[T]](ordering), Quantity(0))
    }

  }

  private[this] def totalQuantity[T <: Tradable](orders: GenIterable[Order[T] with SinglePricePoint[T]]): Quantity = {
    orders.aggregate[Quantity](Quantity(0))((total, order) => Quantity(total.value + order.quantity.value), (q1, q2) => Quantity(q1.value + q2.value))
  }

}
