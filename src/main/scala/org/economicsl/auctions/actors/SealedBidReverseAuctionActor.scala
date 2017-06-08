package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorRef}
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.reverse.SealedBidReverseAuction


class SealedBidReverseAuctionActor[T <: Tradable](reservation: BidOrder[T],
                                                  pricingPolicy: PricingPolicy[T],
                                                  settlementService: ActorRef)
  extends Actor {

  def receive: Receive = {
    case order: AskOrder[T] =>
      auction = auction.insert(order)
    case ClearRequest =>
      val results = auction.clear
      settlementService ! results
      auction = results.residual
  }

  var auction: SealedBidReverseAuction[T] = SealedBidReverseAuction(reservation, pricingPolicy)
  
}
