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

import org.economicsl.auctions.singleunit.pricing.BidQuotePricingRule
import org.economicsl.auctions.Price
import org.scalatest.{FlatSpec, Matchers}


class ReverseAuctionSpec extends FlatSpec with Matchers {

  "A ReverseAuction" should "clear matched orders" in {

    val energy = new Electricity
    val buyer = UUID.randomUUID()
    val reservationPrice = MarketBidOrder(buyer, energy)

    // add a couple of bidders...
    val lowSeller = UUID.randomUUID()
    val lowAskOrder = LimitAskOrder(lowSeller, Price(150), energy)

    val highSeller = UUID.randomUUID()
    val highAskOrder = LimitAskOrder(highSeller, Price(200), energy)

    val auction = ReverseAuction(reservationPrice)
    val withSellers = auction.insert(lowAskOrder).insert(highAskOrder)

    // remove an irrelevant bid order
    val withSingleSeller = withSellers.remove(highAskOrder)

    // clear!
    val lowestPriceWins = new BidQuotePricingRule[Electricity]
    val (results, _) = withSingleSeller.clear(lowestPriceWins)
    results.map(fills => fills.map(fill => fill.price).toList) should be (Some(List(Price(150))))

  }

}
