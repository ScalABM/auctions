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

import org.economicsl.auctions.OrderTracker.{Accepted, Rejected}
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.auctions.{OrderGenerator, TestStock, Token}
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class PeriodicDoubleAuction
    extends FlatSpec
    with Matchers {

  // generate a stream of random orders...
  val google: TestStock = TestStock()
  val prng = new Random(42)
  val orders: Stream[(Token, SingleUnitOrder[TestStock])] = OrderGenerator.randomSingleUnitOrders(0.5)(100, google, prng)

  "A PeriodicDoubleAuction with uniform pricing" should "produce a single price at which all filled orders are processed." in {

    val pricingRule = new MidPointPricingPolicy[TestStock]
    val withUniformPricing = SealedBidAuction.withUniformClearingPolicy(pricingRule, google)

    // this whole process is data parallel...
    val (withOrders, _) = {
      orders.foldLeft((withUniformPricing, Stream.empty[Either[Rejected, Accepted]])){
        case ((auction, insertResults), order) =>
          val (updatedAuction, insertResult) = auction.insert(order)
          (updatedAuction, insertResult #:: insertResults)
      }
    }

    val (clearedAuction, fills) = withOrders.clear
    fills.map(_.map(_.price).toSet).size should be(1)

  }

}
