package org.economicsl.auctions.multiunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.multiunit
import org.economicsl.auctions.{GoogleStock, Price, Quantity}
import org.scalatest.{FlatSpec, Matchers}


class MatchedOrdersSpec extends FlatSpec with Matchers {

  "A set of MatchedOrders" should " accept a " in {
    val google = new GoogleStock

    // Create some multi-unit limit ask orders
    val issuer1 = UUID.randomUUID()
    val order1 = multiunit.LimitAskOrder(issuer1, Price(10), Quantity(10), google)

    val issuer2 = UUID.randomUUID()
    val order2 = multiunit.LimitBidOrder(issuer2, Price(15), Quantity(10), google)

    val empty = MatchedOrders.empty[GoogleStock](multiunit.LimitAskOrder.ordering.reverse, multiunit.LimitBidOrder.ordering.reverse)
    val (matched, optionalUnMatched) = empty + (issuer1 -> order1, issuer2 -> order2)

    assert(matched.nonEmpty)
    optionalUnMatched should be (None)
  }

  "A set of MatchedOrders" should " accept a sdasdf " in {
    val google = new GoogleStock

    // Create some multi-unit limit ask orders
    val issuer1 = UUID.randomUUID()
    val order1 = multiunit.LimitAskOrder(issuer1, Price(10), Quantity(100), google)

    val issuer2 = UUID.randomUUID()
    val order2 = multiunit.LimitBidOrder(issuer2, Price(15), Quantity(10), google)

    // Create an empty set of matched orders and add the newly created orders
    val empty = MatchedOrders.empty[GoogleStock](multiunit.LimitAskOrder.ordering.reverse, multiunit.LimitBidOrder.ordering.reverse)
    val (matched, optionalUnMatched) = empty + (issuer1 -> order1, issuer2 -> order2)

    assert(matched.nonEmpty)
    val rationed = order1.quantity - order2.quantity
    val rationedOrder = order1.withQuantity(rationed)
    optionalUnMatched.map(rationedOrders => rationedOrders.askOrders.head) should be (Some((issuer1, rationedOrder)))

  }


  "A set of MatchedOrders" should " accept a fdgdfg " in {
    val google = new GoogleStock

    // Create some multi-unit limit ask orders
    val issuer1 = UUID.randomUUID()
    val order1 = multiunit.LimitAskOrder(issuer1, Price(10), Quantity(10), google)

    val issuer2 = UUID.randomUUID()
    val order2 = multiunit.LimitBidOrder(issuer2, Price(15), Quantity(100), google)

    // Create an empty set of matched orders and add the newly created orders
    val empty = MatchedOrders.empty[GoogleStock](multiunit.LimitAskOrder.ordering.reverse, multiunit.LimitBidOrder.ordering.reverse)
    val (matched, optionalUnMatched) = empty + (issuer1 -> order1, issuer2 -> order2)

    assert(matched.nonEmpty)
    val rationed = order2.quantity - order1.quantity
    val rationedOrder = order2.withQuantity(rationed)
    optionalUnMatched.map(rationedOrders => rationedOrders.bidOrders.head) should be (Some((issuer2, rationedOrder)))

  }

}
