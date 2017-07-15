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
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy
import org.economicsl.auctions._
import org.economicsl.core.{Currency, Price}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SecondPriceSealedBidAuctionSpec
    extends FlatSpec
    with Matchers
    with TokenGenerator {

  // seller is willing to sell at any positive price...but wants incentive compatible mechanism for buyers!
  val tickSize: Currency = 1
  val secondPriceSealedBidAuction: SealedBidAuction[ParkingSpace] = {
    SealedBidAuction.withUniformClearingPolicy(BidQuotePricingPolicy[ParkingSpace], tickSize)
  }

  val seller: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace()
  val reservationAskOrder: (Token, LimitAskOrder[ParkingSpace]) = (randomToken(), LimitAskOrder(seller, Price.zero, parkingSpace))
  val (withReservationAskOrder, _) = secondPriceSealedBidAuction.insert(reservationAskOrder)

  // suppose that there are lots of bidders
  val prng: Random = new Random(42)
  val numberBidOrders = 1000
  val bidOrders: Stream[(Token, LimitBidOrder[ParkingSpace])] = OrderGenerator.randomBidOrders(1000, parkingSpace, prng)

  // winner should be the bidder that submitted the highest bid
  val (withBidOrders, insertResults) = bidOrders.foldLeft((withReservationAskOrder, Stream.empty[Either[Rejected, Accepted]])) {
    case ((auction, results), bidOrder) =>
      val (updatedAuction, result) = auction.insert(bidOrder)

      (updatedAuction, result #:: results)
  }
  val (clearedAuction, fills): (SealedBidAuction[ParkingSpace], Option[Stream[Fill]]) = withBidOrders.clear

  "A Second-Price, Sealed-Bid Auction (SPSBA)" should "allocate the Tradable to the bidder that submitted the bid with the highest price." in {

    val winner: Option[Buyer] = fills.flatMap(_.headOption.map(_.issuer))
    winner should be(Some(bidOrders.max._2.issuer))

  }

  "The winning price of a Second-Price, Sealed-Bid Auction (SPSBA)" should "be the second-highest submitted bid price" in {

    // winning price from the original auction...
    val winningPrice: Option[Price] = fills.flatMap(_.headOption.map(_.price))

    // remove the winning bid and then find the bid price of the winner of this new auction...
    val withoutHighestPricedBidOrder = bidOrders.filter(???)
    val initialCondition = (withReservationAskOrder, Stream.empty[Either[Rejected, Accepted]])
    val (withBidOrders, insertResults) = withoutHighestPricedBidOrder.foldLeft(initialCondition) {
      case ((auction, results), bidOrder) =>
        val (updatedAuction, result) = auction.insert(bidOrder)
        (updatedAuction, result #:: results)
    }
    val (_, _) = withBidOrders.clear

    // winning price of the original auction should be equal to the limit price of the winner of an auction in which the winner or original auction did not participate!

  }

}
