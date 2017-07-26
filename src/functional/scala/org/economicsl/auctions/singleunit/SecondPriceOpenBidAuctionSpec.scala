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

import org.economicsl.auctions._
import org.economicsl.auctions.quotes.AskPriceQuoteRequest
import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder, SingleUnitBidOrder}
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy
import org.economicsl.core.Price
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SecondPriceOpenBidAuctionSpec
    extends FlatSpec
    with AuctionSimulation
    with Matchers {

  // seller is willing to sell at any positive price...but wants incentive compatible mechanism for buyers!
  val uuid: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(uuid)
  val protocol = AuctionProtocol(parkingSpace)
  val secondPriceOpenBidAuction: OpenBidAuction[ParkingSpace] = {
    OpenBidAuction.withUniformClearingPolicy(BidQuotePricingPolicy[ParkingSpace], protocol)
  }

  val seller: UUID = UUID.randomUUID()
  val sellersToken: Token = UUID.randomUUID()
  val reservationAskOrder: (Token, SingleUnitAskOrder[ParkingSpace]) = (sellersToken, SingleUnitAskOrder(seller, Price.MinValue, parkingSpace))
  val (withReservationAskOrder, _) = secondPriceOpenBidAuction.insert(reservationAskOrder)

  // suppose that there are lots of bidders
  val prng: Random = new Random(42)
  val numberBidOrders = 1000
  val bidOrders: Stream[(Token, SingleUnitBidOrder[ParkingSpace])] = OrderGenerator.randomSingleUnitBidOrders(1000, parkingSpace, prng)
  val (_, highestPricedBidOrder) = bidOrders.maxBy{ case (_, order) => order.limit }

  // winner should be the bidder that submitted the highest bid
  val (withBidOrders, _) = collectOrders[ParkingSpace, OpenBidAuction[ParkingSpace]](withReservationAskOrder)(bidOrders)
  val (clearedAuction, fills): (OpenBidAuction[ParkingSpace], Option[Stream[SpotContract]]) = withBidOrders.clear

  "A Second-Price, Open-Bid Auction (SPOBA)" should "be able to process ask price quote requests" in {

    val issuer: Issuer = UUID.randomUUID()
    val askPriceQuote = withBidOrders.receive(AskPriceQuoteRequest(issuer))
    askPriceQuote.receiver should be(issuer)
    askPriceQuote.quote should be(Some(highestPricedBidOrder.limit))

  }

  "A Second-Price, Open-Bid Auction (SPOBA)" should "allocate the Tradable to the bidder that submitted the bid with the highest price." in {

    val winner: Option[Buyer] = fills.flatMap(_.headOption.map(_.issuer))
    winner should be(Some(highestPricedBidOrder.issuer))

  }

  "The winning price of a Second-Price, Open-Bid Auction (SPOBA)" should "be the second-highest submitted bid price" in {

    val remainingBidOrders = bidOrders.filter{ case (_, order) => order.limit < highestPricedBidOrder.limit }
    val (_, secondHighestPricedBidOrder) = remainingBidOrders.maxBy{ case (_, order) => order.limit }

    val winningPrice: Option[Price] = fills.flatMap(_.headOption.map(_.price))
    winningPrice should be(Some(secondHighestPricedBidOrder.limit))

  }

}
