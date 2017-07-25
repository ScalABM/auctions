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

import java.util.UUID

import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder, SingleUnitBidOrder}
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy
import org.economicsl.auctions._
import org.economicsl.core.Price
import org.scalatest.{FlatSpecLike, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceSealedBidReverseAuctionSpec
    extends AuctionSimulation
    with FlatSpecLike
    with Matchers {

  // reverse auction to procure a service at lowest possible cost...
  val service = Service()
  val firstPriceSealedBidReverseAuction: SealedBidAuction[Service] = {
    SealedBidAuction.withUniformClearingPolicy(BidQuotePricingPolicy[Service], service)
  }

  // buyer is willing to pay anything...
  val buyer: Issuer = UUID.randomUUID()
  val buyersToken: Token = UUID.randomUUID()
  val reservationBidOrder: (Token, SingleUnitBidOrder[Service]) = (buyersToken, SingleUnitBidOrder(buyer, Price.MaxValue, service))
  val (withReservationBidOrder, _) = firstPriceSealedBidReverseAuction.insert(reservationBidOrder)

  // generate some random sellers...
  val numberOffers = 1000
  val prng = new Random(42)
  val offers: Stream[(Token, SingleUnitAskOrder[Service])] = OrderGenerator.randomSingleUnitAskOrders(numberOffers, service, prng)
  val (_, lowestPricedAskOrder): (Token, SingleUnitAskOrder[Service]) = offers.minBy{ case (_, askOrder) => askOrder.limit }

  // insert the ask orders into the auction mechanism...can be done in parallel!
  val (withAskOrders, _) = collectOrders[Service, SealedBidAuction[Service]](withReservationBidOrder)(offers)
  val (clearedAuction, fills) = withAskOrders.clear

  "A First-Price, Sealed-Bid Reverse Auction (FPSBRA)" should "purchse the Service from the seller who offers it at the lowest price." in {

    val winner: Option[Seller] = fills.flatMap(_.headOption.map(_.counterparty))
    winner should be(Some(lowestPricedAskOrder.issuer))

  }

  "The price paid (received) by the buyer (seller) when using a FPSARA" should "be the lowest offered price" in {

    val winningPrice: Option[Price] = fills.flatMap(_.headOption.map(_.price))
    winningPrice should be(Some(lowestPricedAskOrder.limit))

  }

}
