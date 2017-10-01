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

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orders.SingleUnitBidOrder
import org.economicsl.auctions.singleunit.participants.{SingleUnitAuctionParticipant, SingleUnitBuyer, SingleUnitSeller}
import org.economicsl.core.{Price, Tradable}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceSealedBidAuctionSimulation
  extends SingleUnitAuctionSimulation
  with FlatSpecLike
  with Matchers {

  // define some tradable...
  val tradable = ParkingSpace()

  // single-seller, multiple-buyers...
  val sellersValuation: Map[Tradable, Price] = Map(tradable -> Price.MinValue)
  val seller = SingleUnitSeller(sellersValuation)
  val numberBuyers = 10
  val buyers: IndexedSeq[SingleUnitBuyer] = for (i <- 1 to numberBuyers) yield {
    val valuation: Price = Price(Random.nextInt(100))
    val buyersValuation: Map[Tradable, Price] = Map(tradable -> valuation)
    SingleUnitBuyer(buyersValuation)
  }
  val auctionParticipants: IndexedSeq[SingleUnitAuctionParticipant] = seller +: buyers

  // use factory method to create instance of first-priced sealed bid auction...
  val auction: SealedBidAuction[ParkingSpace] = firstPriceSealedBidAuction(tradable)


  val issuedOrders: Iterable[IssuedOrder[ParkingSpace]] = issueOrders(auction.protocol, auctionParticipants)
  val bidOrders: Iterable[SingleUnitBidOrder[ParkingSpace]] = issuedOrders.collect {
    case (_, (_, order: SingleUnitBidOrder[ParkingSpace])) => order
  }
  val ((clearedAuction, _), contracts) = run[ParkingSpace, SealedBidAuction[ParkingSpace]](auction, auctionParticipants)

  "A first-price, sealed-bid auction (FPSBA)" should "allocate the parking space to the bidder that submits the bid with the highest price." in {

    val actualWinner: Option[Issuer] = contracts.flatMap(_.headOption.map(_.issuer))
    val highestPricedBidOrder = bidOrders.maxBy(bidOrder => bidOrder.limit)
    val expectedWinner: Option[Issuer] = Some(highestPricedBidOrder.issuer)
    actualWinner should be(expectedWinner)

  }

  "The winning price of a first-price, sealed-bid auction (FPSBA)" should "be the highest submitted bid price." in {

    val actualWinningPrice: Option[Price] = contracts.flatMap(_.headOption.map(_.price))
    val highestPricedBidOrder = bidOrders.maxBy(bidOrder => bidOrder.limit)
    val expectedWinningPrice: Option[Price] = Some(highestPricedBidOrder.limit)
    actualWinningPrice should be(expectedWinningPrice)

  }

}
