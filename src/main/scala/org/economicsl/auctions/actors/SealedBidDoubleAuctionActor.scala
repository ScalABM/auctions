package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.twosided.SealedBidDoubleAuction
import org.economicsl.core.Tradable

import scala.util.{Failure, Success}


final class SealedBidDoubleAuctionActor[T <: Tradable] private(pricingPolicy: PricingPolicy[T],
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

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.fills.foreach(fills => settlementService ! fills)
      auction = results.residual
  }

  def receive: Receive = {
    handleOrder orElse handleClearRequest
  }

  private[this] var auction: SealedBidDoubleAuction.DiscriminatoryPricingImpl[T] = {
    SealedBidDoubleAuction.withDiscriminatoryPricing(pricingPolicy, tickSize)
  }

}


object SealedBidDoubleAuctionActor {

  def props[T <: Tradable](pricingPolicy: PricingPolicy[T],
                           tickSize: Long,
                           settlementService: ActorRef): Props = {
    Props(new SealedBidDoubleAuctionActor[T](pricingPolicy, tickSize, settlementService))
  }

}
