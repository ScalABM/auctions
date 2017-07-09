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

import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.auctions.{Fill, ParkingSpace, Token}
import org.economicsl.core.{Currency, Price}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class FirstPriceOpenBidAuctionSpec extends FlatSpec with Matchers with BidOrderGenerator {

  import AuctionParticipant._

  // Seller that must sell at any positive price
  val seller: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace()
  val token: Token = randomToken()
  val reservation: (Token, LimitAskOrder[ParkingSpace]) = (token, LimitAskOrder(seller, Price.MinValue, parkingSpace))

  // suppose that there are lots of bidders
  val prng: Random = new Random(42)
  val numberBidOrders = 10000
  val bids: Stream[(Token, LimitBidOrder[ParkingSpace])] = randomBidOrders(numberBidOrders, parkingSpace, prng)


  // seller uses a first-priced, sealed bid auction...
  val tickSize: Currency = 1
  val fpoba: FirstPriceOpenBidAuction[ParkingSpace] = FirstPriceOpenBidAuction.withTickSize(tickSize)
  val (withReservation, response) = fpoba.insert(reservation)

  response match {
    case Left(Rejected(_, _, _, _)) =>
      ???
    case Right(Accepted(_, _, _, _)) =>
      ???
  }

   val (_, highestPricedBidOrder) = bids.max

  // withBids will include all accepted bids (this is trivially parallel..)
  val withBids: FirstPriceOpenBidAuction[ParkingSpace] = bids.foldLeft(withReservation) {
    case (auction, bidOrder) =>
      val (updated, bidderResponses) = auction.insert(bidOrder)
      updated
  }

  val (residual, fills): (FirstPriceOpenBidAuction[ParkingSpace], Option[Stream[Fill]]) = withBids.clear

  "A First-Price, Open-Bid Auction (FPOBA)" should "be able to process ask price quote requests" in {

    val askPriceQuote = withBids.receive(new AskPriceQuoteRequest)
    askPriceQuote should be(AskPriceQuote(Some(highestPricedBidOrder.limit)))

  }

  "A First-Price, Open-Bid Auction (FPOBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price." in {

    fills.map(_.map(_.issuer)) should be(Some(Stream(highestPricedBidOrder.issuer)))

  }

  "The winning price of a First-Price, Open-Bid Auction (FPOBA)" should "be the highest submitted bid price." in {

    fills.map(_.map(_.price)) should be(Some(Stream(highestPricedBidOrder.limit)))

  }

}
