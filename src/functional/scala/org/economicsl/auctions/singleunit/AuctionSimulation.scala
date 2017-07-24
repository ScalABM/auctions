package org.economicsl.auctions.singleunit

import org.economicsl.auctions.OrderTracker.{Accepted, Rejected}
import org.economicsl.auctions.Token
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable


trait AuctionSimulation {

  type InsertResult[T <: Tradable, A <: Auction[T, A]] = (A, Stream[Either[Rejected, Accepted]])

  def insert[T <: Tradable, A <: Auction[T, A]](initial: A)(orders: Stream[(Token, SingleUnitOrder[T])]): (A, Stream[Either[Rejected, Accepted]]) = {
    orders.foldLeft((initial, Stream.empty[Either[Rejected, Accepted]])) {
      case ((auction, insertResults), order) =>
        val (updated, insertResult) = auction.insert(order)
        (updated, insertResult #:: insertResults)
    }
  }

}
