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

import org.economicsl.auctions.singleunit.AuctionParticipant.{Accepted, Rejected}
import org.economicsl.auctions.singleunit.orders.{BidOrder, LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy
import org.economicsl.auctions.{Issuer, Service, Token}
import org.economicsl.core.Price
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SecondPriceSealedBidReverseAuctionSpec
    extends FlatSpec
    with Matchers {

  // reverse auction to procure a service at lowest possible cost...
  val secondPriceSealedBidReverseAuction: SealedBidAuction[Service] = {
    SealedBidAuction.withUniformClearingPolicy(AskQuotePricingPolicy[Service])
  }

  // buyer is willing to pay anything...
  val buyer: Issuer = UUID.randomUUID()
  val buyersToken: Token = UUID.randomUUID()
  val service = Service()
  val reservationBidOrder: (Token, BidOrder[Service]) = (buyersToken, LimitBidOrder(buyer, Price.MaxValue, service))
  val (withReservationBidOrder, _) = secondPriceSealedBidReverseAuction.insert(reservationBidOrder)

  // generate some random sellers...
  val prng = new Random(42)
  val offers: Stream[(Token, LimitAskOrder[Service])] = OrderGenerator.randomAskOrders(1000, service, prng)
  val (_, lowestPricedAskOrder): (Token, LimitAskOrder[Service]) = offers.min

  // insert the ask orders into the auction mechanism...can be done in parallel!
  val (withAskOrders, _): (SealedBidAuction[Service], Stream[Either[Rejected, Accepted]]) = {
    offers.foldLeft((withReservationBidOrder, Stream.empty[Either[Rejected, Accepted]])) {
      case ((auction, insertResults), askOrder) =>
        val (updatedAuction, insertResult) = auction.insert(askOrder)
        (updatedAuction, insertResult #:: insertResults)
    }
  }

  val (clearedAuction, fills) = withAskOrders.clear

  "A Second-Price, Sealed-Ask Reverse Auction (SPSBRA)" should "purchase the Service from the seller who offers it at the lowest price." in {

    val winner: Option[Issuer] = fills.flatMap(_.headOption.map(_.issuer))
    winner should be(Some(lowestPricedAskOrder.issuer))

  }

  "The price paid (received) by the buyer (seller) when using a SPSBRA" should "be the second-lowest offered price" in {
    ???
  }

}
