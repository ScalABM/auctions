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

import org.economicsl.auctions.singleunit.pricing.AskQuotePricingRule
import org.economicsl.auctions.Price
import org.scalatest.{FlatSpec, Matchers}


class AuctionSpec extends FlatSpec with Matchers {
  
  "An Auction" should "clear matched orders" in {

    val guernica = new Guernica
    val seller = UUID.randomUUID()
    val reservationPrice =LimitAskOrder(seller, Price(10), guernica)

    // add a couple of bidders...
    val lowBidder = UUID.randomUUID()
    val lowBidOrder = LimitBidOrder(lowBidder, Price(15), guernica)

    val highBidder = UUID.randomUUID()
    val highBidOrder = LimitBidOrder(highBidder, Price(100), guernica)

    val auction = Auction.withReservationPrice(reservationPrice)
    val withBidders = auction.insert(lowBidOrder).insert(highBidOrder)

    // remove an irrelevant bid order
    val withSingleBidder = withBidders.remove(lowBidOrder)

    // clear!
    val highestPriceWins = new AskQuotePricingRule[Guernica]
    val (results, _) = withSingleBidder.withPricingRule(highestPriceWins).clear
    results.map(fills => fills.map(fill => fill.price).toList) should be (Some(List(Price(100))))

  }

}
