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
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class PeriodicDoubleAuction extends FlatSpec with Matchers with OrderGenerator {

  // generate a stream of random orders...
  type Orders[T <: Tradable] = Either[LimitAskOrder[T], LimitBidOrder[T]]
  val google: GoogleStock = GoogleStock(tick=1)
  val prng = new Random(42)
  val orders: Stream[Orders[GoogleStock]] = {
    randomOrders(100, google, prng)
  }

  "A PeriodicDoubleAuction with uniform pricing" should "produce a single price at which all filled orders are processed." in {

    val pricingRule = new MidPointPricingPolicy[GoogleStock]
    val withUniformPricing: DoubleAuction[GoogleStock] = DoubleAuction.withUniformPricing(pricingRule)

    val withOrders: DoubleAuction[GoogleStock] = orders.foldLeft(withUniformPricing) { case (auction, order) =>
      order match {
        case Left(askOrder) => auction.insert(askOrder)
        case Right(bidOrder) => auction.insert(bidOrder)
      }
    }

    val (results, _) = withOrders.clear
    results.map(fills => fills.map(fill => fill.price).toSet).size should be(1)

  }

}
