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

import org.economicsl.auctions._
import org.economicsl.auctions.messages._
import org.economicsl.core.Quantity
import org.economicsl.core.util.UUIDGenerator
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
    with UUIDGenerator {

  val tradable = TestTradable()

  val numberBids = 100
  val bidReferences: Iterable[OrderReferenceId] = for (i <- 0 until numberBids) yield randomUUID()
  val bids: Stream[SingleUnitBid[TestTradable]] = NewOrderGenerator.randomSingleUnitBids(numberBids, tradable, new Random(42))

  val numberOffers = 100
  val offerReferences: Iterable[OrderReferenceId] = for (i <- 0 until numberOffers) yield randomUUID()
  val offers: Stream[SingleUnitOffer[TestTradable]] = NewOrderGenerator.randomSingleUnitOffers(numberOffers, tradable, new Random(42))

  val bidOrdering: Ordering[SingleUnitBid[TestTradable]] = SingleUnitBid.priceOrdering
  val offerOrdering: Ordering[SingleUnitOffer[TestTradable]] = SingleUnitOffer.priceOrdering
  val initial: FourHeapOrderBook[TestTradable] = FourHeapOrderBook.empty[TestTradable](bidOrdering, offerOrdering)
  val withBids: FourHeapOrderBook[TestTradable] = bidReferences.zip(bids).foldLeft(initial){
    case (orderBook, (reference, bidOrder)) => orderBook + (reference -> bidOrder)
  }
  val withOffers: FourHeapOrderBook[TestTradable] = offerReferences.zip(offers).foldLeft(initial){
    case (orderBook, (reference, askOrder)) => orderBook + (reference -> askOrder)
  }

  "A FourHeapOrderBook" should "be able to insert bid orders" in {

    withBids.matchedOrders.numberUnits should be(Quantity.zero)

    withBids.unMatchedOrders.offers.numberUnits should be(Quantity.zero)
    withBids.unMatchedOrders.bids.numberUnits should be(Quantity(numberBids))

  }

  "A FourHeapOrderBook" should "be able to insert ask orders" in {

    withOffers.matchedOrders.numberUnits should be(Quantity.zero)

    withOffers.unMatchedOrders.offers.numberUnits should be(Quantity(numberOffers))
    withOffers.unMatchedOrders.bids.numberUnits should be(Quantity.zero)

  }

  "A FourHeapOrderBook" should "be able to remove ask orders" in {

    val withOutOffers = offerReferences.foldLeft(withOffers) {
      case (orderBook, reference) =>
        val (residual, _) = orderBook - reference
        residual
    }
    assert(withOutOffers.unMatchedOrders.offers.isEmpty)

  }

  "A FourHeapOrderBook" should "be able to remove bid orders" in {

    val withOutBids = bidReferences.foldLeft(withBids){
      case (orderBook, reference) =>
        val (residual, _) = orderBook - reference
        residual
    }
    assert(withOutBids.unMatchedOrders.bids.isEmpty)

  }

  "A FourHeapOrderBook" should "be able to combine with another FourHeapOrderBook" in {

    val withOrders = withBids.combineWith(withOffers)
    val expectedNumberUnits = withBids.numberUnits + withOffers.numberUnits
    withOrders.numberUnits should be(expectedNumberUnits)

  }

}
