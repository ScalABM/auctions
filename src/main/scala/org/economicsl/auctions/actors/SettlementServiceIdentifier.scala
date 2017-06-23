package org.economicsl.auctions.actors

import akka.actor.{ActorIdentity, ActorRef, ReceiveTimeout}

import scala.concurrent.duration.Duration


/** This trait will eventually reside in the settlement package. */
trait SettlementServiceIdentifier {
  this: StackableActor =>

  import AuctionRegistry._

  /** Path to the `SettlementService` (could be local or remote). */
  def settlementServicePath: String

  /** Duration that an auction actor should wait for `SettlementService` to be available before shutting down. */
  def timeout: Duration

  /** Attempt to identify the location of the `SettlementService` (which could be local or remote!). */
  def identifying: Receive = {
    case ActorIdentity("settlementService", registry) =>
      registry match {
        case Some(actorRef) =>
          context.watch(actorRef)
          actorRef ! RegisterAuction(self)
          settlementService = Some(actorRef)
          context.setReceiveTimeout(Duration.Undefined)  // in case setReceiveTimeout has been previously called!
        case None =>
          context.setReceiveTimeout(timeout)
          identify(settlementServicePath, "settlementService")
      }
    case ReceiveTimeout =>
      context.stop(self)
  }

  /* Reference for auction registry (initialized to `None`) */
  protected var settlementService: Option[ActorRef] = None

}
