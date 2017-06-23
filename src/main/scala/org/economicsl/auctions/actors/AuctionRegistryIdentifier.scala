package org.economicsl.auctions.actors

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, ReceiveTimeout}

import scala.concurrent.duration.Duration


trait AuctionRegistryProvider {
  this: StackableActor =>

  import AuctionRegistry._

  /** Path to the `AuctionRegistry` (could be local or remote). */
  def auctionRegistryPath: String

  /** Duration that `AuctionParticipantActor` should wait for `AuctionRegistry` to be available before shutting down. */
  def timeout: Duration

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
          context.setReceiveTimeout(timeout)
          identify(auctionRegistryPath, "auctionRegistry")
      }
    case ReceiveTimeout =>
      context.stop(self)
  }

  /* Reference for auction registry (initialized to `None`) */
  protected var auctionRegistry: Option[ActorRef] = None

  protected def identify(path: String, messageId: Any): Unit = {
    context.actorSelection(path) ! Identify(messageId)
  }

}
