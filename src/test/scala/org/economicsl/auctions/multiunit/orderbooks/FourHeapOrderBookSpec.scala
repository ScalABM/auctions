package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.multiunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{GoogleStock, Price, Quantity, multiunit}
import org.scalatest.{FlatSpec, Matchers}


class FourHeapOrderBookSpec extends FlatSpec with Matchers {

  "An empty FourHeapOrderBook" should "accept a new order" in {

    val google = new GoogleStock

    // Create some multi-unit limit ask orders
    val issuer1 = UUID.randomUUID()
    val order1 = LimitAskOrder(issuer1, Price(10), Quantity(10), google)

    val issuer2 = UUID.randomUUID()
    val order2 = LimitBidOrder(issuer2, Price(15), Quantity(10), google)

    // Create an empty orderBook
    val empty = FourHeapOrderBook.empty[GoogleStock]

    // Add a limit ask order
    val uuid1 = UUID.randomUUID()
    val orderBook = empty + (uuid1, order1)
    assert(orderBook.nonEmpty)

    // add a limit bid Order
    val uuid2 = UUID.randomUUID()
    val orderBook2 = empty + (uuid2, order2)
    assert(orderBook2.nonEmpty)

    val orderBook3 = empty + (uuid1, order1) + (uuid2, order2)
    assert(orderBook.nonEmpty)

  }

}
