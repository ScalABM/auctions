package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorRef, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable




trait AuctionActor[T <: Tradable, A <: Auction[T, A]]
    extends Actor {

  import AuctionActor._
  import AuctionParticipantActor._

  var auction: A

  def settlementService: ActorRef

  /**
    *
    * @return
    * @todo
    */
  def registeringAuctionParticipants: Receive = {
    case RegisterAuctionParticipant(participant) =>
      context.watch(participant)
      participant ! AuctionProtocol(auction.tickSize, tradable)
      participants = participants + participant
      ticker = ticker.addRoutee(participant)
    case DeregisterAuctionParticipant(participant) =>
      context.unwatch(participant)
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
    case Terminated(participant) if participants.contains(participant) =>
      context.unwatch(participant)
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
  }

  def receive: Receive = {
    case InsertOrder(token, order) =>
      val (updatedAuction, insertResult) = auction.insert(token -> order)
      insertResult match {
        case Right(accepted) =>
          sender() ! accepted
          auction = updatedAuction
        case Left(rejected) =>
          sender() ! rejected
      }
    case CancelOrder(reference) =>
      val (updatedAuction, cancelResult) = auction.cancel(reference)
      cancelResult match {
        case Some(canceled) =>
          sender() ! canceled
          auction = updatedAuction
        case None =>
          ???  // some kind of message indicated that cancel attempt was unsuccessful...
      }
  }

  // Not sure that it is necessary to store refs...
  protected var participants = Set.empty[ActorRef]

  /* Router will broadcast messages to all registered auction participants (even if participants are remote!) */
  protected var ticker: Router = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee])

}


object AuctionActor {

  final case class CancelOrder(reference: Reference)

  final case class InsertOrder[T <: Tradable](token: Token, order: Order[T])

  final case class DeregisterAuctionParticipant(participant: ActorRef)

  final case class RegisterAuctionParticipant(participant: ActorRef)

}