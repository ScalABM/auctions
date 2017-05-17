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
package org.economicsl.auctions.singleunit.twosided

import java.util.UUID

import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingPolicy
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{ParkingSpace, Price}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class OpenBidDoubleAuctionSpec extends FlatSpec with Matchers {

  val pricingRule = new WeightedAveragePricingPolicy[ParkingSpace](weight = 0.5)
  val withDiscriminatoryPricing: OpenBidDoubleAuction.DiscriminatoryPricingImpl[ParkingSpace] = OpenBidDoubleAuction.withDiscriminatoryPricing(pricingRule)
  val withUniformPricing: OpenBidDoubleAuction.UniformPricingImpl[ParkingSpace] = OpenBidDoubleAuction.withUniformPricing(pricingRule)

  val prng = new Random(42)

  "A DoubleAuction (DA)" should "generate the same number of fills as orders." in {

    val parkingSpace = ParkingSpace(tick = 1)

    // how many ask and bid orders should be generated...
    val numberOrders = 100

    // create some ask orders...
    val offers = {
      for (i <- 1 to numberOrders) yield {
        val price = Price(prng.nextInt(Int.MaxValue))
        LimitAskOrder(UUID.randomUUID(), price, parkingSpace)
      }
    }

    // create some bid orders (making sure that there will be no rationing)...
    val bids = {
      for (i <- 1 to numberOrders) yield {
        val price = offers.max.limit + Price(prng.nextInt(Int.MaxValue))
        LimitBidOrder(UUID.randomUUID(), price, parkingSpace)
      }
    }

    // insert all of the orders...
    val withBids = bids.foldLeft(withDiscriminatoryPricing)((auction, bidOrder) => auction.insert(bidOrder))
    val withOrders = offers.foldLeft(withBids)((auction, askOrder) => auction.insert(askOrder))

    // without rationing, the number of fills should match the number of orders
    val results = withOrders.clear
    results.fills.map(_.length) should be(Some(numberOrders))

  }

}
