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
package org.economicsl.auctions.singleunit.orderbooks

import java.util.UUID

import org.economicsl.auctions.singleunit.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{Price, Quantity, TestTradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FourHeapOrderBookSpec extends FlatSpec with Matchers {

  // suppose that seller must sell the parking space at any positive price...
  val tradable = TestTradable(tick = 1)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val bids: Iterable[LimitBidOrder[TestTradable]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitBidOrder(UUID.randomUUID(), price, tradable)
    }
  }

  val offers: Iterable[LimitAskOrder[TestTradable]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitAskOrder(UUID.randomUUID(), price, tradable)
    }
  }

  val initial: FourHeapOrderBook[TestTradable] = FourHeapOrderBook.empty[TestTradable]
  val withBids: FourHeapOrderBook[TestTradable] = bids.foldLeft(initial)((orderBook, bidOrder) => orderBook.insert(bidOrder))
  val withOffers: FourHeapOrderBook[TestTradable] = offers.foldLeft(initial)((orderBook, askOrder) => orderBook.insert(askOrder))

  "A FourHeapOrderBook" should "be able to insert bid orders" in {

    withBids.matchedOrders.askOrders.numberUnits should be(Quantity(0))
    withBids.matchedOrders.bidOrders.numberUnits should be(Quantity(0))

    withBids.unMatchedOrders.askOrders.numberUnits should be(Quantity(0))
    withBids.unMatchedOrders.bidOrders.numberUnits should be(Quantity(100))

    // insert bids into a non-empty order book
    val withOrders = bids.foldLeft(withOffers)((orderBook, bidOrder) => orderBook.insert(bidOrder))

  }

  "A FourHeapOrderBook" should "be able to insert ask orders" in {

    withOffers.matchedOrders.askOrders.numberUnits should be(Quantity(0))
    withOffers.matchedOrders.bidOrders.numberUnits should be(Quantity(0))

    withOffers.unMatchedOrders.askOrders.numberUnits should be(Quantity(100))
    withOffers.unMatchedOrders.bidOrders.numberUnits should be(Quantity(0))

    // insert offers into a non-empty order book
    val withOrders = offers.foldLeft(withBids)((orderBook, askOrder) => orderBook.insert(askOrder))

  }

  "A FourHeapOrderBook" should "be able to remove ask orders" in {

    val withOutOffers = offers.foldLeft(withOffers)((orderBook, askOrder) => orderBook.remove(askOrder))
    assert(withOutOffers.unMatchedOrders.askOrders.isEmpty)

  }

  "A FourHeapOrderBook" should "be able to remove bid orders" in {

    val withOutBids = bids.foldLeft(withBids)((orderBook, bidOrder) => orderBook.remove(bidOrder))
    assert(withOutBids.unMatchedOrders.bidOrders.isEmpty)

  }

}
