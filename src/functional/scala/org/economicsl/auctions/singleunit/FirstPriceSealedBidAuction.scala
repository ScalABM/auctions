/*
Copyright 2017 EconomicSL

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

import org.economicsl.auctions.{Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceSealedBidAuction extends FlatSpec with Matchers {

  // suppose that seller must sell the parking space at any positive price...
  val seller: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(tick = 1)

  val reservationPrice = LimitAskOrder(seller, Price.MinValue, parkingSpace)
  val fpsba: Auction[ParkingSpace] = Auction.firstPriceSealedBid(reservationPrice)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val bids: Iterable[LimitBidOrder[ParkingSpace]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitBidOrder(UUID.randomUUID(), price, parkingSpace)
    }
  }

  @annotation.tailrec
  final def insert[T <: Tradable](orders: Iterable[LimitBidOrder[T]], auction: Auction[T]): Auction[T] = {
    if (orders.isEmpty) auction else insert(orders.tail, auction.insert(orders.head))
  }

  val (results, _) = insert(bids, fpsba).clear

  "A First-Price, Sealed-Bid Auction (FPSBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price." in {

    results.map(fills => fills.map(fill => fill.bidOrder.issuer)) should be(Some(Stream(bids.max.issuer)))

  }

  "The winning price of a First-Price, Sealed-Bid Auction (FPSBA)" should "be the highest submitted bid price." in {

    results.map(fills => fills.map(fill => fill.bidOrder.limit)) should be(Some(Stream(bids.max.limit)))

  }

}
