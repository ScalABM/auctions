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

import org.economicsl.auctions.ParkingSpace
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingPolicy
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.core.Price
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Random, Success}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidDoubleAuctionSpec extends FlatSpec with Matchers {

  val pricingRule = new WeightedAveragePricingPolicy[ParkingSpace](weight = 0.5)
  val withDiscriminatoryPricing: SealedBidDoubleAuction.DiscriminatoryPricingImpl[ParkingSpace] = {
    SealedBidDoubleAuction.withDiscriminatoryPricing(pricingRule, tickSize = 1)
  }
  val withUniformPricing: SealedBidDoubleAuction.UniformPricingImpl[ParkingSpace] = {
    SealedBidDoubleAuction.withUniformPricing(pricingRule, tickSize = 1)
  }

  val prng = new Random(42)

  "A Sealed-Bid, DoubleAuction (SBDA)" should "generate the same number of fills as orders." in {

    val parkingSpace = ParkingSpace(UUID.randomUUID())

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
    val withBids = bids.foldLeft(withDiscriminatoryPricing){ case (auction, bidOrder) =>
      auction.insert(bidOrder) match {
        case Success(withBid) => withBid
        case _ => auction
      }
    }
    val withOrders = offers.foldLeft(withBids){ case (auction, askOrder) =>
      auction.insert(askOrder) match {
        case Success(withAsk) => withAsk
        case _ => auction
      }
    }

    // without rationing, the number of fills should match the number of orders
    val results = withOrders.clear
    results.contracts.map(_.length) should be(Some(numberOrders))

  }

}
