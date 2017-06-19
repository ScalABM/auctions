package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.economicsl.auctions.singleunit.SealedBidAuction
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.Tradable

import scala.util.{Failure, Success}


final class SealedBidAuctionActor[T <: Tradable] private(reservation: AskOrder[T],
                                                         pricingPolicy: PricingPolicy[T],
                                                         tickSize: Long,
                                                         settlementService: ActorRef)
    extends Actor
    with ActorLogging
    with Timestamper {

  def timestamp(): Long = {
    currentTimeMillis()
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

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.fills.foreach(fills => settlementService ! fills)
      auction = results.residual
  }


  def receive: Receive = {
    handleBidOrder orElse handleClearRequest
  }

  private[this] var auction: SealedBidAuction[T] = SealedBidAuction(reservation, pricingPolicy, tickSize)
  
}


object SealedBidAuctionActor {

  def props[T <: Tradable](reservation: AskOrder[T],
                           pricingPolicy: PricingPolicy[T],
                           tickSize: Long,
                           settlementService: ActorRef): Props = {
    Props(new SealedBidAuctionActor[T](reservation, pricingPolicy, tickSize, settlementService))
  }

}