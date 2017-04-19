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
package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingRule
import org.economicsl.auctions.{ParkingSpace, Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class ClosedDoubleAuction extends FlatSpec with Matchers {

  // suppose that seller must sell the parking space at any positive price...
  val parkingSpace = ParkingSpace(tick = 1)

  val pricingRule = new WeightedAveragePricingRule[ParkingSpace](weight = 0.5)
  val emptyAuction: DoubleAuction[ParkingSpace] = DoubleAuction.withDiscriminatoryPricing(pricingRule)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val bids: Iterable[LimitBidOrder[ParkingSpace]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitBidOrder(UUID.randomUUID(), price, parkingSpace)
    }
  }

  val offers: Iterable[LimitAskOrder[ParkingSpace]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitAskOrder(UUID.randomUUID(), price, parkingSpace)
    }
  }

  @annotation.tailrec
  final def insert1[T <: Tradable](orders: Iterable[LimitBidOrder[T]], auction: DoubleAuction[T]): DoubleAuction[T] = {
    if (orders.isEmpty) auction else insert1(orders.tail, auction.insert(orders.head))
  }

  @annotation.tailrec
  final def insert2[T <: Tradable](orders: Iterable[LimitAskOrder[T]], auction: DoubleAuction[T]): DoubleAuction[T] = {
    if (orders.isEmpty) auction else insert2(orders.tail, auction.insert(orders.head))
  }

  // todo this is a bit awkward to work with!
  val withOrders: DoubleAuction[ParkingSpace] = insert2(offers, insert1(bids, emptyAuction))
  withOrders
  val (results, _) = withOrders.clear

  "A DoubleAuction (DA)" should "allocate the Tradable units to the bidders that submit the bids with the highest prices." in {

    results.foreach(fills => println(fills.map(fill => (0.5 * fill.askOrder.limit.value + 0.5 * fill.bidOrder.limit.value).round).toList))
    results.foreach(fills => println(fills.map(fill => fill.price.value).toList))
    results.map(fills => fills.length) should be(100)

  }

}
