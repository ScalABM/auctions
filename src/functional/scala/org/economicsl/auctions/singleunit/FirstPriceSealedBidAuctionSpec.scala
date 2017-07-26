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
class FirstPriceSealedBidAuctionSpec
    extends FlatSpec
    with Matchers
    with AuctionSimulation
    with TokenGenerator {

  // seller uses a first-priced, sealed bid auction...
  val uuid: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(uuid)
  val protocol = AuctionProtocol(parkingSpace)
  val firstPriceSealedBidAuction: SealedBidAuction[ParkingSpace] = {
    SealedBidAuction.withUniformClearingPolicy(AskQuotePricingPolicy[ParkingSpace], protocol)
  }

  // suppose that seller must sell the parking space at any positive price...
  val seller: Issuer = UUID.randomUUID()
  val token: Token = randomToken()
  val order = SingleUnitAskOrder(seller, Price.MinValue, parkingSpace)
  val reservationAskOrder: (Token, SingleUnitAskOrder[ParkingSpace]) = (token, order)
  val (withReservationAskOrder, _) = firstPriceSealedBidAuction.insert(reservationAskOrder)

  // suppose that there are lots of bidders
  val prng: Random = new Random(42)
  val numberBidOrders = 1000
  val bidOrders: Stream[(Token, SingleUnitBidOrder[ParkingSpace])] = {
    OrderGenerator.randomSingleUnitBidOrders(numberBidOrders, parkingSpace, prng)
  }
  val (_, highestPricedBidOrder) = bidOrders.maxBy{ case (_, bidOrder) => bidOrder.limit }

  val (withBidOrders, _) = collectOrders[ParkingSpace, SealedBidAuction[ParkingSpace]](withReservationAskOrder)(bidOrders)
  val (clearedAuction, fills) = withBidOrders.clear

  "A first-price, sealed-bid auction (FPSBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price." in {

    val winner: Option[Buyer] = fills.flatMap(_.headOption.map(_.issuer))
    winner should be(Some(highestPricedBidOrder.issuer))

  }

  "The winning price of a first-price, sealed-bid auction (FPSBA)" should "be the highest submitted bid price." in {

    val winningPrice: Option[Price] = fills.flatMap(_.headOption.map(_.price))
    winningPrice should be(Some(highestPricedBidOrder.limit))

  }

}
