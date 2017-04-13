package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Currency, Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceSealedBidAuction extends FlatSpec with Matchers {

  "A First-Price, Sealed-Bid Auction (FPSBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price" in {

    case class ParkingSpace(tick: Currency) extends Tradable

    // suppose that seller must sell the parking space at any positive price...
    val seller = UUID.randomUUID()
    val parkingSpace = ParkingSpace(tick = 1)

    val reservationPrice = MarketAskOrder(seller, parkingSpace)
    val fpsba = Auction.firstPriceSealedBid(reservationPrice)

    // suppose that there are lots of bidders
    val prng = new Random(42)
    val bids = for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(1000))
      LimitBidOrder(UUID.randomUUID(), price, parkingSpace)
    }

    @annotation.tailrec
    def insert[T <: Tradable](orders: Iterable[LimitBidOrder[T]], auction: Auction[T]): Auction[T] = {
      if (orders.isEmpty) auction else insert(orders.tail, auction.insert(orders.head))
    }

    // winner should be the bidder that submitted the highest bid
    val (results, _) = insert(bids, fpsba).clear
    results.map(fills => fills.map(fill => fill.bidOrder)) should be (Some(Stream(bids.min)))  // this is weird want to write bids.max

  }

}
