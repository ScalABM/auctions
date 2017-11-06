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
import org.economicsl.auctions.singleunit.orders.SingleUnitAskOrder
import org.economicsl.auctions.singleunit.participants.{SingleUnitAuctionParticipant, SingleUnitBuyer, SingleUnitSeller}
import org.economicsl.core.{Price, Tradable}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceSealedBidReverseAuctionSimulation
  extends SingleUnitAuctionSimulation
  with FlatSpecLike
  with Matchers {

  // define some tradable...
  val tradable = Service()

  // single-buyer, multiple-sellers...
  val buyersValuation: Map[Tradable, Price] = Map(tradable -> Price.MaxValue)
  val buyer = SingleUnitBuyer(buyersValuation)
  val numberSellers = 10
  val sellers: IndexedSeq[SingleUnitSeller] = for (i <- 1 to numberSellers) yield {
    val valuation: Price = Price(Random.nextInt(100))
    val sellersValuation: Map[Tradable, Price] = Map(tradable -> valuation)
    SingleUnitSeller(sellersValuation)
  }
  val auctionParticipants: IndexedSeq[SingleUnitAuctionParticipant] = buyer +: sellers

  // use factory method to create instance of first-priced sealed bid reverse auction...
  val auctionId: AuctionId = UUID.randomUUID()
  val auction: SealedBidAuction[Service] = firstPriceSealedBidReverseAuction(auctionId, tradable)

  val issuedOrders: Iterable[IssuedOrder[Service]] = issueOrders(auction.protocol, auctionParticipants)
  val askOrders: Iterable[SingleUnitAskOrder[Service]] = issuedOrders.collect {
    case (_, (_, order: SingleUnitAskOrder[Service])) => order
  }
  val ((clearedAuction, _), contracts) = run[Service, SealedBidAuction[Service]](auction, auctionParticipants)

  "A first-price, sealed-bid reverse auction (FPSBRA)" should "allocate the parking space to the bidder that submits the offer with the lowest price." in {

    val actualWinner: Option[Issuer] = contracts.flatMap(_.headOption.map(_.counterparty))
    val lowestPricedAskOrder = askOrders.minBy(order => order.limit)
    val expectedWinner: Option[Issuer] = Some(lowestPricedAskOrder.issuer)
    actualWinner should be(expectedWinner)

  }

  "The winning price of a first-price, sealed-bid reverse auction (FPSBRA)" should "be the lowest submitted offer price." in {

    val actualWinningPrice: Option[Price] = contracts.flatMap(_.headOption.map(_.price))
    val lowestPricedAskOrder = askOrders.minBy(order => order.limit)
    val expectedWinningPrice: Option[Price] = Some(lowestPricedAskOrder.limit)
    actualWinningPrice should be(expectedWinningPrice)

  }

}
