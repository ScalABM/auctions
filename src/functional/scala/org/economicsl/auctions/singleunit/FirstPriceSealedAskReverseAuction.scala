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

import org.economicsl.auctions.{Price, Service}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceSealedAskReverseAuction extends FlatSpec with Matchers with AskOrderGenerator {

  // suppose that buyer must procure some service...
  val buyer: UUID = UUID.randomUUID()
  val service = Service(tick=1)

  val reservationPrice = LimitBidOrder(buyer, Price.MaxValue, service)
  val fpsara: ReverseAuction[Service] = ReverseAuction.firstPriceSealedAsk(reservationPrice)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val offers: Stream[LimitAskOrder[Service]] = randomAskOrders(1000, service, prng)

  val withAsks: ReverseAuction[Service] = offers.foldLeft(fpsara)((auction, askOrder) => auction.insert(askOrder))
  val (results, _) = withAsks.clear

  "A First-Price, Sealed-Ask Reverse Auction (FPSARA)" should "purchse the Service from the seller who offers it at the lowest price." in {

    results.map(fills => fills.map(fill => fill.askOrder.issuer)) should be (Some(Stream(offers.min.issuer)))

  }

  "The price paid (received) by the buyer (seller) when using a FPSARA" should "be the lowest offered price" in {

    results.map(fills => fills.map(fill => fill.price)) should be (Some(Stream(offers.min.limit)))

  }

}