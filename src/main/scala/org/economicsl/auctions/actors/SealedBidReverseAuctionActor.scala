package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.reverse.SealedBidReverseAuction
import org.economicsl.core.Tradable

import scala.util.{Failure, Success}


final class SealedBidReverseAuctionActor[T <: Tradable] private(reservation: BidOrder[T],
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

  def clearingBehavior: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.fills.foreach(fills => settlementService ! fills)
      auction = results.residual
  }


  def receive: Receive = {
    handleAskOrder orElse clearingBehavior
  }

  private[this] var auction: SealedBidReverseAuction[T] = SealedBidReverseAuction(reservation, pricingPolicy, tickSize)

}


object SealedBidReverseAuctionActor {

  def props[T <: Tradable](reservation: BidOrder[T],
                           pricingPolicy: PricingPolicy[T],
                           tickSize: Long,
                           settlementService: ActorRef): Props = {
    Props(new SealedBidReverseAuctionActor[T](reservation, pricingPolicy, tickSize, settlementService))
  }

}