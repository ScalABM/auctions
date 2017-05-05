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
package org.economicsl.auctions.singleunit.reverse

import java.util.UUID

import org.economicsl.auctions.singleunit.{AskOrderGenerator, LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{Price, Service}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class SecondPriceSealedAskReverseAuction extends FlatSpec with Matchers with AskOrderGenerator {

  // suppose that buyer must procure some service...
  val buyer: UUID = UUID.randomUUID()
  val service = Service(tick=1)

  val reservationPrice = LimitBidOrder(buyer, Price.MaxValue, service)
  val spsara: ReverseAuction[Service] = ReverseAuction.secondPriceSealedAsk(reservationPrice)

  // suppose that there are lots of sellers
  val prng = new Random(42)
  val offers: Stream[LimitAskOrder[Service]] = randomAskOrders(1000, service, prng)


  val auction: ReverseAuction[Service] = offers.foldLeft(spsara)((auction, askOrder) => auction.insert(askOrder))
  val results: ClearResult[Service, ReverseAuction[Service]] = auction.clear

  "A Second-Price, Sealed-Ask Reverse Auction (SPSARA)" should "purchase the Service from the seller who offers it at the lowest price." in {

    val winner = results.fills.map(_.map(_.askOrder.issuer))
    winner should be(Some(Stream(offers.min.issuer)))

  }

  "The price paid (received) by the buyer (seller) when using a SPSARA" should "be the second-lowest offered price" in {

    // winning price from the original auction...
    val winningPrice = results.fills.map(_.map(_.price))

    // remove the winning offer and then find the ask price of the winner of this new auction...
    val auction2 = auction.remove(offers.min)
    val results2 = auction2.clear
    results2.fills.map(_.map(_.askOrder.limit)) should be (winningPrice)

  }

}
