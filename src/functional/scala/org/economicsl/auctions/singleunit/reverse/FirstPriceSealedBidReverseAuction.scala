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

import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.singleunit.AskOrderGenerator
import org.economicsl.auctions.{ClearResult, Service}
import org.economicsl.core.Price
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Random, Success}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceSealedBidReverseAuction extends FlatSpec with Matchers with AskOrderGenerator {

  // suppose that buyer must procure some service...
  val buyer: UUID = UUID.randomUUID()
  val service = Service(UUID.randomUUID())

  val reservationPrice = LimitBidOrder(buyer, Price.MaxValue, service)
  val fpsara: SealedBidReverseAuction[Service] = SealedBidReverseAuction.withBidQuotePricingPolicy(reservationPrice, tickSize = 1)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val offers: Stream[LimitAskOrder[Service]] = randomAskOrders(1000, service, prng)

  val withAsks: SealedBidReverseAuction[Service] = offers.foldLeft(fpsara){ case (auction, askOrder) =>
    auction.insert(askOrder) match {
      case Success(withAsk) => withAsk
      case _ => auction
    }
  }
  val results: ClearResult[SealedBidReverseAuction[Service]] = withAsks.clear

  "A First-Price, Sealed-Bid Reverse Auction (FPSBRA)" should "purchse the Service from the seller who offers it at the lowest price." in {

    results.contracts.map(_.map(_.counterparty)) should be (Some(Stream(offers.min.issuer)))

  }

  "The price paid (received) by the buyer (seller) when using a FPSARA" should "be the lowest offered price" in {

    results.contracts.map(_.map(_.price)) should be (Some(Stream(offers.min.limit)))

  }

}
