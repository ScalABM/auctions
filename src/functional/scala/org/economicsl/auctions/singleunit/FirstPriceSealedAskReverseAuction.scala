package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Currency, Price, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random


class FirstPriceSealedAskReverseAuction extends FlatSpec with Matchers {

  "A First-Price, Sealed-Ask Reverse Auction (FPSARA)" should "allocate the Tradable to the seller that submits the ask with the lowest price" in {

    case class ParkingSpace(tick: Currency) extends Tradable

    // suppose that seller must sell the parking space at any positive price...
    val seller = UUID.randomUUID()
    val parkingSpace = ParkingSpace(tick = 1)

    val reservationPrice = LimitBidOrder(seller, Price.MaxValue, parkingSpace)
    val fpsara = ReverseAuction.firstPriceSealedAsk(reservationPrice)

    // suppose that there are lots of bidders
    val prng = new Random(42)
    val offers = for (i <- 1 to 100) yield {
      val price = Price(prng.nextInt(1000))
      LimitAskOrder(UUID.randomUUID(), price, parkingSpace)
    }

    @annotation.tailrec
    def insert[T <: Tradable](orders: Iterable[LimitAskOrder[T]], auction: ReverseAuction[T]): ReverseAuction[T] = {
      if (orders.isEmpty) auction else insert(orders.tail, auction.insert(orders.head))
    }

    // winner should be the seller that submitted the lowest ask
    val (results, _) = insert(offers, fpsara).clear
    results.map(fills => fills.map(fill => fill.askOrder)) should be (Some(Stream(offers.min)))

  }

}
