package org.economicsl.auctions.actors

import akka.actor.{ActorIdentity, ActorRef, ReceiveTimeout, Terminated}
import org.economicsl.core.{Currency, Tradable}

import scala.concurrent.duration.Duration


trait AuctionRegistryIdentifier {
  this: StackableActor =>

  import AuctionRegistry._
  import AuctionRegistryIdentifier._

  /** Path to the `AuctionRegistry` (could be local or remote). */
  def auctionRegistryPath: String

  /** Duration that `AuctionParticipantActor` should wait for `AuctionRegistry` to be available before shutting down. */
  def auctionRegistryTimeout: Duration

  /** Attempt to identify the location of the `AuctionRegistry` (which could be local or remote!). */
  def identifyingAuctionRegistry: Receive = {
    case ActorIdentity("auctionRegistry", registry) =>
      registry match {
        case Some(actorRef) =>
          context.watch(actorRef)
          actorRef ! RegisterAuctionParticipant(self)
          auctionRegistry = Some(actorRef)
          context.setReceiveTimeout(Duration.Undefined)  // in case setReceiveTimeout has been previously called!
        case None =>
          context.setReceiveTimeout(auctionRegistryTimeout)
          identify(auctionRegistryPath, "auctionRegistry")
      }
    case ReceiveTimeout =>
      context.stop(self)
  }

  /** Upon receipt of a `RegisterAuction` message from the `AuctionRegistry` containing the `auctionRefs` for each of
    * the currently available auctions, the `AuctionParticipant` will send a `RegisterAuctionParticipant` message to
    * each of these `auctionRefs` in order to obtain the respective `AuctionProtocol` instances. These `AuctionProtocol`
    * instances should contain all relevant information about the structure and rules of a particular auction necessary
    * for the `AuctionParticipant` to generate and submit valid orders.
    */
  def registeringAuctions: Receive = {
    case RegisterAuction(auctionRefs) =>
      auctionRefs.foreach{ auctionRef =>
        context.watch(auctionRef)
        auctionRef ! RegisterAuctionParticipant(self)
      }
    case Terminated(actorRef) if auctions.contains(actorRef) =>
      context.unwatch(actorRef)
      auctions = auctions - actorRef
    case protocol : AuctionProtocol =>
      auctions = auctions + (sender() -> protocol)
  }

  /* Reference for auction registry (initialized to `None`) */
  protected var auctionRegistry: Option[ActorRef] = None

  /* An `AuctionParticipant` needs to keep track of multiple auction protocols. */
  protected var auctions: Map[AuctionRef, AuctionProtocol] = Map.empty

}


object AuctionRegistryIdentifier {

  /** Need some data structure to convey the information about an auction to participants. */
  final case class AuctionProtocol(tickSize: Currency, tradable: Tradable)

}