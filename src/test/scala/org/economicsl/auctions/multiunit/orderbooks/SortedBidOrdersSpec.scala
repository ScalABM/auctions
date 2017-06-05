package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.multiunit.orders
import org.economicsl.auctions.multiunit.orders.LimitBidOrder
import org.economicsl.auctions.{GoogleStock, Price, Quantity, multiunit}
import org.scalatest.{FlatSpec, Matchers}


class SortedBidOrdersSpec extends FlatSpec with Matchers {

  "A SortedBidOrderBook" should "update an existing order" in {

    // Create a multi-unit limit ask order
    val issuer = UUID.randomUUID()
    val google = new GoogleStock
    val order = LimitBidOrder(issuer, Price(10), Quantity(100), google)

    // Create an empty order book and add the order
    val empty = SortedBidOrders.empty[GoogleStock]
    val nonEmpty = empty + (issuer -> order)
    nonEmpty.head should be ((issuer, order))

    // Create a revised order and update the order book
    val revised = order.withQuantity(Quantity(1))
    val updated = nonEmpty.updated(issuer, revised)

    // Check that update is successful!
    updated.head should be ((issuer, revised))
    updated.size should be (1)
    updated.numberUnits should be (revised.quantity)

  }

  "A SortedBidOrderBook" should "split itself into two pieces" in {

    val google = new GoogleStock

    // Create some multi-unit limit bid orders
    val issuer1 = UUID.randomUUID()
    val order1 = orders.LimitBidOrder(issuer1, Price(10), Quantity(10), google)

    val issuer2 = UUID.randomUUID()
    val order2 = orders.LimitBidOrder(issuer2, Price(5), Quantity(15), google)

    val issuer3 = UUID.randomUUID()
    val order3 = orders.LimitBidOrder(issuer3, Price(15), Quantity(100), google)

    // Create an empty order book and add the orders
    val empty = SortedBidOrders.empty[GoogleStock]
    val nonEmpty = empty + (issuer1 -> order1) + (issuer2 -> order2) + (issuer3 -> order3)

    // Create a revised order and update the order book
    val (matched, residual) = nonEmpty.splitAt(Quantity(57))

    // Check that splitAt was successful
    matched.numberUnits should be (Quantity(57))
    matched.size should be(1)

    residual.numberUnits should be (Quantity(125 - 57))
    residual.size should be(3)

  }

}
