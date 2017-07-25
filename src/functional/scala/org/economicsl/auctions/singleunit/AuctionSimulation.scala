package org.economicsl.auctions.singleunit

import org.economicsl.auctions.OrderTracker.{Accepted, Rejected}
import org.economicsl.auctions.Token
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable


trait AuctionSimulation {

  type OrderFlow[+T <: Tradable] = Stream[(Token, SingleUnitOrder[T])]

  type Result[T <: Tradable, A <: Auction[T, A]] = (A, Stream[Either[Rejected, Accepted]])

  def collectOrders[T <: Tradable, A <: Auction[T, A]](auction: A)(orders: OrderFlow[T]): Result[T, A] = {
    val initialResult: Result[T, A] = (auction, Stream.empty[Either[Rejected, Accepted]])
    orders.aggregate(initialResult)({
      case ((currentAuction, results), order) =>
        val (updatedAuction, result) = currentAuction.insert(order)
        (updatedAuction, result #:: results)
    }, {
      case ((auction1, results1), (auction2, results2)) =>
        (auction1.combineWith(auction2), results1.append(results2))
    })
  }

}
