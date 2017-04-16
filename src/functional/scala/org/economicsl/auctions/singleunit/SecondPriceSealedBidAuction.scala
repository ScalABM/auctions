package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Currency, Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class SecondPriceSealedBidAuction extends FlatSpec with Matchers {

  "A Second-Price, Sealed-Bid Auction (SPSBA)" should "allocate the Tradable to the bidder that submits the bid with the highest price" in {

    case class ParkingSpace(tick: Currency) extends Tradable

    // suppose that seller must sell the parking space at any positive price...
    val seller = UUID.randomUUID()
    val parkingSpace = ParkingSpace(tick = 1)

    // seller is willing to sell at any positive price
    val reservationPrice = LimitAskOrder(seller, Price.MinValue, parkingSpace)
    val spsba = Auction.secondPriceSealedBid(reservationPrice)

    // suppose that there are lots of bidders
    val prng = new Random(42)
    val bids = for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(Int.MaxValue))
      LimitBidOrder(UUID.randomUUID(), price, parkingSpace)
    }

    @annotation.tailrec
    def insert[T <: Tradable](orders: Iterable[LimitBidOrder[T]], auction: Auction[T]): Auction[T] = {
      if (orders.isEmpty) auction else insert(orders.tail, auction.insert(orders.head))
    }

    // winner should be the bidder that submitted the highest bid
    val auction = insert(bids, spsba)
    val (results, _) = auction.clear
    val winningBids = results.map(fills => fills.map(fill => fill.bidOrder))
    val winningPrice = results.map(fills => fills.map(fill => fill.price))
    winningBids should be (Some(Stream(bids.max)))

    // winning price should be the price of the second highest bid
    val auction2 = auction.remove(bids.max)
    val (results2, _) = auction2.clear
    results2.map(fills => fills.map(fill => fill.bidOrder.limit)) should be (winningPrice)

  }

}
