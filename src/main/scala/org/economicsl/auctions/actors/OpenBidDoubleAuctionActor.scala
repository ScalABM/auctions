package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.economicsl.auctions.quotes.{AskPriceQuoteRequest, BidPriceQuoteRequest, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.twosided.OpenBidDoubleAuction
import org.economicsl.core.Tradable

import scala.util.{Failure, Success}


final class OpenBidDoubleAuctionActor[T <: Tradable](pricingPolicy: PricingPolicy[T],
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

  def handleBidOrder: Receive = {
    case order: BidOrder[T] =>
      auction.insert(order) match {
        case Success(updated) =>
          sender() ! Accepted(timestamp(), order)
          auction = updated
        case Failure(ex) =>
          sender() ! Rejected(timestamp(), order, ex)
      }
  }

  def handleOrder: Receive = {
    handleAskOrder orElse handleBidOrder
  }

  def handleAskPriceQuoteRequest: Receive = {
    case quote: AskPriceQuoteRequest[T] =>
      sender() ! auction.receive(quote)
  }

  def handleBidPriceQuoteRequest: Receive = {
    case quote: BidPriceQuoteRequest[T] =>
      sender() ! auction.receive(quote)
  }

  def handleSpreadQuoteRequest: Receive = {
    case quote: SpreadQuoteRequest[T] =>
      sender() ! auction.receive(quote)
  }

  def handleQuoteRequest: Receive = {
    handleAskPriceQuoteRequest orElse handleBidPriceQuoteRequest orElse handleSpreadQuoteRequest
  }

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.fills.foreach(fills => settlementService ! fills)
      auction = results.residual
  }

  def receive: Receive = {
    handleOrder orElse handleQuoteRequest orElse handleClearRequest
  }

  private[this] var auction: OpenBidDoubleAuction.DiscriminatoryPricingImpl[T] = {
    OpenBidDoubleAuction.withDiscriminatoryPricing(pricingPolicy, tickSize)
  }

}
