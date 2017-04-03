package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.{GoogleStock, Price, Quantity, multiunit}
import org.scalatest.{FlatSpec, Matchers}


class SortedAskOrdersSpec extends FlatSpec with Matchers {

  "A SortedAskOrderBook" should "update an existing order" in {

    // Create a multi-unit limit ask order
    val issuer = UUID.randomUUID()
    val google = new GoogleStock
    val order = multiunit.LimitAskOrder(issuer, Price(10), Quantity(100), google)

    // Create an empty order book and add the order
    val empty = SortedAskOrders.empty[GoogleStock]
    val nonEmpty = empty + (issuer -> order)
    nonEmpty.head should be ((issuer, order))

    // Create a revised order and update the order book
    val revised = order.withQuantity(Quantity(1))
    val updated = nonEmpty.updated(issuer, revised)

    // Check that update is successful!
    updated.head should be ((issuer, revised))
    updated.size should be (1)
    updated.quantity should be (revised.quantity)

  }

}
