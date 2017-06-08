package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorRef}
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.BidPriceQuoteRequest
import org.economicsl.auctions.singleunit.reverse.OpenBidReverseAuction
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


class OpenBidReverseAuctionActor[T <: Tradable](reservation: BidOrder[T],
                                                pricingPolicy: PricingPolicy[T],
                                                settlementService: ActorRef)
  extends Actor {

  def receive: Receive = {
    case request: BidPriceQuoteRequest[T] =>
      sender() ! auction.receive(request)
    case order: AskOrder[T] =>
      auction = auction.insert(order)
    case ClearRequest =>
      val results = auction.clear
      settlementService ! results
      auction = results.residual
  }

  var auction: OpenBidReverseAuction[T] = OpenBidReverseAuction(reservation, pricingPolicy)
  
}
