package org.economicsl.auctions.actors

import akka.actor.{ActorRef, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import org.economicsl.auctions.{Reference, Token}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable




trait AuctionActor[T <: Tradable, A <: Auction[T, A]]
    extends StackableActor {

  import AuctionActor._

  override def receive: Receive = {
    case InsertOrder(token, order: SingleUnitOrder[T]) =>
      val (updatedAuction, response) = auction.insert(token -> order)
      response match {
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
          /* indicates that the reference was not found in the auction; could mean that CancelOrder was sent to wrong
          AuctionActor, or, depending on the ClearingStrategy used by the AuctionActor, that the order corresponding
          to the reference was cleared prior to the AuctionActor processing the CancelOrder message.
          */
          ???
      }
    case RegisterAuctionParticipant(participant) =>
      context.watch(participant)  // `AuctionActor` will be notified if `AuctionParticipantActor` "dies"...
      participant ! auction.protocol
      participants = participants + participant
      ticker = ticker.addRoutee(participant)
    case DeregisterAuctionParticipant(participant) =>
      context.unwatch(participant)  // `AuctionActor` will no longer be notified if `AuctionParticipantActor` "dies"...
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
    case Terminated(participant) if participants.contains(participant) =>
      context.unwatch(participant)  // `AuctionParticipantActor` has actually died!
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
  }

  protected var auction: A

  // Not sure that it is necessary to store refs...
  protected var participants = Set.empty[ActorRef]

  /* Router will broadcast messages to all registered auction participants (even if participants are remote!) */
  protected var ticker: Router = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee])

}


object AuctionActor {

  final case class CancelOrder(reference: Reference)

  final case class InsertOrder[T <: Tradable](token: Token, order: SingleUnitOrder[T])

  final case class DeregisterAuctionParticipant(participant: ActorRef)

  final case class RegisterAuctionParticipant(participant: ActorRef)

}