package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.economicsl.auctions.quotes.BidPriceQuoteRequest
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.reverse.OpenBidReverseAuction
import org.economicsl.core.Tradable

import scala.util.{Failure, Success}


final class OpenBidReverseAuctionActor[T <: Tradable] private(reservation: BidOrder[T],
                                                              pricingPolicy: PricingPolicy[T],
                                                              tickSize: Long,
                                                              settlementService: ActorRef)
    extends Actor
    with ActorLogging
    with Timestamper {

  def timestamp(): Long = {
    currentTimeMillis()
  }

  def handleAskOrder: Receive = {
    case order: AskOrder[T] =>
      auction.insert(order) match {
        case Success(updated) =>
          sender() ! Accepted(timestamp(), order)
          auction = updated
        case Failure(ex) =>
          sender() ! Rejected(timestamp(), order, ex)
      }
  }

  def handleBidPriceQuoteRequest: Receive = {
    case quote: BidPriceQuoteRequest[T] =>
      sender() ! auction.receive(quote)
  }

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.fills.foreach(fills => settlementService ! fills)
      auction = results.residual
  }

  def receive: Receive = {
    handleAskOrder orElse handleBidPriceQuoteRequest orElse handleClearRequest
  }

  private[this] var auction: OpenBidReverseAuction[T] = OpenBidReverseAuction(reservation, pricingPolicy, tickSize)

}


object OpenBidReverseAuctionActor {

  def props[T <: Tradable](reservation: BidOrder[T],
                           pricingPolicy: PricingPolicy[T],
                           tickSize: Long,
                           settlementService: ActorRef): Props = {
    Props(new OpenBidReverseAuctionActor[T](reservation, pricingPolicy, tickSize, settlementService))
  }

}