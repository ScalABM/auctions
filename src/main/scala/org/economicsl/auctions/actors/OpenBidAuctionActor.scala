package org.economicsl.auctions.actors

import akka.actor.Props
import org.economicsl.auctions.quotes.{AskPriceQuoteRequest, BidPriceQuoteRequest, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


class OpenBidAuctionActor[T <: Tradable] private(protected var auction: OpenBidAuction[T])
    extends AuctionActor[T, OpenBidAuction[T]] {

  def handleQuoteRequest: Receive = {
    case request: AskPriceQuoteRequest[T] =>
      sender() ! auction.receive(request)
    case request: BidPriceQuoteRequest[T] =>
      sender() ! auction.receive(request)
    case request: SpreadQuoteRequest[T] =>
      sender() ! auction.receive(request)
  }

}


object OpenBidAuctionActor {

  def withDiscriminatoryClearingPolicy[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): Props = {
    val auction = OpenBidAuction.withDiscriminatoryClearingPolicy(pricingPolicy, tickSize)
    Props(new OpenBidAuctionActor(auction))
  }

  def withUniformClearingPolicy[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): Props = {
    val auction = OpenBidAuction.withUniformClearingPolicy(pricingPolicy, tickSize)
    Props(new OpenBidAuctionActor(auction))
  }

}