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

import org.economicsl.auctions.{ParkingSpace, Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class SecondPriceSealedAskReverseAuction extends FlatSpec with Matchers {

  // suppose that seller must sell the parking space at any positive price...
  val seller: UUID = UUID.randomUUID()
  val parkingSpace = ParkingSpace(tick = 1)

  // seller is willing to sell at any positive price
  val reservationPrice = LimitBidOrder(seller, Price.MaxValue, parkingSpace)
  val spsbra: ReverseAuction[ParkingSpace] = ReverseAuction.secondPriceSealedAsk(reservationPrice)

  // suppose that there are lots of bidders
  val prng = new Random(42)
  val offers: Iterable[LimitAskOrder[ParkingSpace]] = {
    for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitAskOrder(UUID.randomUUID(), price, parkingSpace)
    }
  }

  @annotation.tailrec
  final def insert[T <: Tradable](orders: Iterable[LimitAskOrder[T]], auction: ReverseAuction[T]): ReverseAuction[T] = {
    if (orders.isEmpty) auction else insert(orders.tail, auction.insert(orders.head))
  }

  val auction: ReverseAuction[ParkingSpace] = insert(offers, spsbra)
  val (results, _) = auction.clear

  "A Second-Price, Sealed-Ask Reverse Auction (SPSBRA)" should "allocate the Tradable to the seller that submitted the ask with the lowest price." in {

    val winner = results.map(fills => fills.map(fill => fill.askOrder.issuer))
    winner should be(Some(Stream(offers.min.issuer)))

  }

  "The winning price of a Second-Price, Sealed-Ask Reverse Auction (SPSBRA)" should "be the second-lowest submitted ask price" in {

    // winning price from the original auction...
    val winningPrice = results.map(fills => fills.map(fill => fill.price))

    // remove the winning offer and then find the ask price of the winner of this new auction...
    val auction2 = auction.remove(offers.min)
    val (results2, _) = auction2.clear
    results2.map(fills => fills.map(fill => fill.askOrder.limit)) should be (winningPrice)

  }

}
