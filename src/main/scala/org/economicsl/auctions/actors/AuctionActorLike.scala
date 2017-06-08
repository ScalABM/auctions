package org.economicsl.auctions.actors

import akka.actor.Actor
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orders.BidOrder


trait AuctionActorLike[T <: Tradable] {
  this: Actor =>

  def auctionLikeBehavior: Receive = {
    case order: BidOrder[T] => auction = auction.insert(order)
    case OrderCancellationRequest(order) => auction.remove(order)
  }

}
