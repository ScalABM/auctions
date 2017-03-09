package org.economicsl.auctions

import scala.collection.immutable


package object orderbooks {

  class SortedAskOrders[T <: Tradable] private(orders: immutable.TreeSet[LimitAskOrder[T]], val numberUnits: Quantity) {

    def + (order: LimitAskOrder[T]): SortedAskOrders[T] = {
      new SortedAskOrders(orders + order, Quantity(numberUnits.value + order.quantity.value))
    }

    def - (order: LimitAskOrder[T]): SortedAskOrders[T] = {
      new SortedAskOrders(orders - order, Quantity(numberUnits.value - order.quantity.value))
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

    def - (order: LimitBidOrder[T]): SortedBidOrders[T] = {
      new SortedBidOrders(orders - order, Quantity(numberUnits.value - order.quantity.value))
    }

    def contains(order: LimitBidOrder[T]): Boolean = orders.contains(order)

    def  head: LimitBidOrder[T] = orders.head

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

}
