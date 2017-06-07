package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.multiunit.orders.LimitAskOrder
import org.economicsl.auctions.{GoogleStock, Price, Quantity}
import org.scalatest.{FlatSpec, Matchers}


class SortedAskOrdersSpec extends FlatSpec with Matchers {

  "A SortedAskOrderBook" should "be able to add a new ask order" in {

    // Create a multi-unit limit ask order
    val issuer = UUID.randomUUID()
    val google = GoogleStock(tick = 1)
    val order = LimitAskOrder(issuer, Price(10), Quantity(100), google)

    // Create an empty order book and add the order
    val empty = SortedAskOrders.empty[(Price, UUID), GoogleStock]
    empty.numberUnits should be(Quantity(0))
    empty.contains((order.limit, order.issuer)) should be(false)

    val nonEmpty = empty + ((order.limit, order.issuer),  order)
    nonEmpty.headOption should be (Some(((order.limit, order.issuer), order)))
    nonEmpty.numberUnits should be(Quantity(100))
    nonEmpty.contains((order.limit, order.issuer)) should be(true)

  }

  "A SortedAskOrderBook" should "be able to remove an existing ask order" in {

    // Create a multi-unit limit ask order
    val issuer = UUID.randomUUID()
    val google = GoogleStock(tick = 1)
    val order = LimitAskOrder(issuer, Price(10), Quantity(100), google)

    // Create an empty order book, add the order, and then remove it
    val empty = SortedAskOrders.empty[(Price, UUID), GoogleStock]
    val nonEmpty = empty + ((order.limit, order.issuer), order)
    val withoutOrders = nonEmpty - ((order.limit, order.issuer), order)
    withoutOrders.isEmpty should be(true)

  }


}
