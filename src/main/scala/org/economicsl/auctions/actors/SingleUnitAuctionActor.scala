package org.economicsl.auctions.actors

import org.economicsl.auctions.actors.AuctionActor.InsertOrder
import org.economicsl.auctions.singleunit.SingleUnitAuction
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable


trait SingleUnitAuctionActor[T <: Tradable, A <: SingleUnitAuction[T, A]]
  extends AuctionActor[T, SingleUnitOrder[T], A] {

  override def receive: Receive = {
    case message @ InsertOrder(token, order: SingleUnitOrder[T]) =>
      val (updatedAuction, response) = auction.insert(token -> order)
      response match {
        case Right(accepted) =>
          sender() ! accepted
          auction = updatedAuction
        case Left(rejected) =>
          sender() ! rejected
      }
      super.receive(message)
  }

}
