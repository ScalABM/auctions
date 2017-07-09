/*
Copyright (c) 2017 KAPSARC

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

import org.economicsl.auctions.{Reference, ReferenceGenerator, TestTradable, Token}
import org.economicsl.auctions.singleunit.{AskOrderGenerator, BidOrderGenerator}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.core.Quantity
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FourHeapOrderBookSpec
    extends FlatSpec
    with Matchers
    with AskOrderGenerator
    with BidOrderGenerator
    with ReferenceGenerator {

  val tradable = TestTradable()

  val numberBids = 100
  val bidReferences: Iterable[Reference] = for (i <- 0 until numberBids) yield randomReference()
  val bids: Stream[(Token, LimitBidOrder[TestTradable])] = randomBidOrders(numberBids, tradable, new Random(42))

  val numberOffers = 100
  val offerReferences: Iterable[Reference] = for (i <- 0 until numberOffers) yield randomReference()
  val offers: Stream[(Token, LimitAskOrder[TestTradable])] = randomAskOrders(numberOffers, tradable, new Random(42))

  val initial: FourHeapOrderBook[TestTradable] = FourHeapOrderBook.empty[TestTradable]
  val withBids: FourHeapOrderBook[TestTradable] = bidReferences.zip(bids).foldLeft(initial){
    case (orderBook, (reference, bidOrder)) => orderBook.insert(reference -> bidOrder)
  }
  val withOffers: FourHeapOrderBook[TestTradable] = offerReferences.zip(offers).foldLeft(initial){
    case (orderBook, (reference, askOrder)) => orderBook.insert(reference -> askOrder)
  }


  "A FourHeapOrderBook" should "be able to insert bid orders" in {

    withBids.matchedOrders.numberUnits should be(Quantity.zero)

    withBids.unMatchedOrders.askOrders.numberUnits should be(Quantity.zero)
    withBids.unMatchedOrders.bidOrders.numberUnits should be(Quantity(numberBids))

  }

  "A FourHeapOrderBook" should "be able to insert ask orders" in {

    withOffers.matchedOrders.numberUnits should be(Quantity.zero)

    withOffers.unMatchedOrders.askOrders.numberUnits should be(Quantity(numberOffers))
    withOffers.unMatchedOrders.bidOrders.numberUnits should be(Quantity.zero)

  }

  "A FourHeapOrderBook" should "be able to remove ask orders" in {

    val withOutOffers = offerReferences.foldLeft(withOffers) {
      case (orderBook, reference) =>
        val (residual, removedAskOrders) = orderBook.remove(reference)
        residual
    }
    assert(withOutOffers.unMatchedOrders.askOrders.isEmpty)

  }

  "A FourHeapOrderBook" should "be able to remove bid orders" in {

    val withOutBids = bidReferences.foldLeft(withBids){
      case (orderBook, reference) =>
        val (residual, removedBidOrders) = orderBook.remove(reference)
        residual
    }
    assert(withOutBids.unMatchedOrders.bidOrders.isEmpty)

  }

}
