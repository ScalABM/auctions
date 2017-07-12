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

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.AuctionParticipant.{Accepted, Rejected}
import org.economicsl.auctions.singleunit.orders.Order
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
  val google: GoogleStock = GoogleStock()
  val prng = new Random(42)
  val orders: Stream[(Token, Order[GoogleStock])] = OrderGenerator.randomOrders(0.5)(100, google, prng)

  "A PeriodicDoubleAuction with uniform pricing" should "produce a single price at which all filled orders are processed." in {

    val pricingRule = new MidPointPricingPolicy[GoogleStock]
    val withUniformPricing = SealedBidAuction.withUniformClearingPolicy(pricingRule, tickSize = 1)

    // this whole process is data parallel...
    val results = {
      orders.foldLeft((Stream.empty[Either[Rejected, Accepted]], withUniformPricing)){
        case ((responses, auction), order) =>
          val (withOrder, response) = auction.insert(order)
          (response #:: responses, withOrder)
      }
    }

    val (_, withOrders) = results
    withOrders.clear.fills.map(_.map(_.price).toSet).size should be(1)

  }

}
