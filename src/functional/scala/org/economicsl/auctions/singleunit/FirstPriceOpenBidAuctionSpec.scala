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

import org.economicsl.auctions.quotes.AskPriceQuoteRequest
import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder$, SingleUnitBidOrder}
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy
import org.economicsl.auctions._
import org.economicsl.core.Price
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceOpenBidAuctionSpec
    extends FlatSpec
    with Matchers
    with AuctionSimulation
    with TokenGenerator {

  // suppose that there are lots of bidders
  val numberBidOrders = 10000
  val uuid: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(uuid)
  val prng: Random = new Random(42)
  val bidOrders: Stream[(Token, SingleUnitBidOrder[ParkingSpace])] = OrderGenerator.randomBidOrders(numberBidOrders, parkingSpace, prng)
  val (_, highestPricedBidOrder) = bidOrders.maxBy{ case (_, bidOrder) => bidOrder.limit }

  // seller uses a first-priced, open bid auction...
  val firstPriceOpenBidAuction: OpenBidAuction[ParkingSpace] = {
    OpenBidAuction.withUniformClearingPolicy(AskQuotePricingPolicy[ParkingSpace], parkingSpace)
  }

  // Seller that must sell at any positive price
  val seller: UUID = UUID.randomUUID()
  val token: Token = randomToken()
  val reservation: (Token, SingleUnitAskOrder[ParkingSpace]) = (token, SingleUnitAskOrder(seller, Price.MinValue, parkingSpace))
  val (withReservationAskOrder, _) = firstPriceOpenBidAuction.insert(reservation)

  // withBidOrders will include all accepted bids (this is trivially parallel..)
  val (withBidOrders, _) = insert[ParkingSpace, OpenBidAuction[ParkingSpace]](withReservationAskOrder)(bidOrders)
  val (clearedAuction, clearResults) = withBidOrders.clear

  "A First-Price, Open-Bid Auction (FPOBA)" should "be able to process ask price quote requests" in {

    val issuer = highestPricedBidOrder.issuer
    val askPriceQuote = withBidOrders.receive(AskPriceQuoteRequest(issuer))
    askPriceQuote.receiver should be (issuer)
    askPriceQuote.quote should be (Some(highestPricedBidOrder.limit))

  }

  "A first-price, open-bid auction (FPSBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price." in {

    val winner: Option[Issuer] = clearResults.flatMap(_.headOption.map(_.issuer))
    winner should be(Some(highestPricedBidOrder.issuer))

  }

  "The winning price of a first-price, open-bid auction (FPSBA)" should "be the highest submitted bid price." in {

    val winningPrice: Option[Price] = clearResults.flatMap(_.headOption.map(_.price))
    winningPrice should be(Some(highestPricedBidOrder.limit))

  }


}
