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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{ParkingSpace, Token}
import org.economicsl.auctions.singleunit.AuctionParticipant.{Accepted, Rejected}
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingPolicy
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidDoubleAuctionSpec extends FlatSpec with Matchers {

  val pricingRule = new WeightedAveragePricingPolicy[ParkingSpace](weight = 0.5)
  val withDiscriminatoryPricing: SealedBidAuction[ParkingSpace] = {
    SealedBidAuction.withDiscriminatoryClearingPolicy(pricingRule)
  }
  val withUniformPricing: SealedBidAuction[ParkingSpace] = {
    SealedBidAuction.withUniformClearingPolicy(pricingRule)
  }

  val prng = new Random(42)

  "A Sealed-Bid, DoubleAuction (SBDA)" should "generate the same number of fills as orders." in {

    val parkingSpace = ParkingSpace()

    // how many ask and bid orders should be generated...
    val numberOrders = 100

    // create some ask orders...
    val orders: Stream[(Token, Order[ParkingSpace])] = OrderGenerator.randomOrders(0.5)(100, parkingSpace, prng)

    // this whole process is data parallel...
    val (withOrders, _) = {
      orders.foldLeft((withUniformPricing, Stream.empty[Either[Rejected, Accepted]])){
        case ((auction, insertResults), order) =>
          val (updatedAuction, insertResult) = auction.insert(order)
          (updatedAuction, insertResult #:: insertResults)
      }
    }

    // without rationing, the number of fills should match the number of orders
    val (clearedAuction, fills) = withOrders.clear
    fills.map(_.length) should be(Some(numberOrders))

  }

}
