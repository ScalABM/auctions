package org.economicsl.auctions.actors

import akka.actor.{ActorIdentity, ActorRef, ReceiveTimeout, Terminated}

import scala.concurrent.duration.Duration


/** This trait will eventually reside in the settlement package. */
trait SettlementServiceParticipant
    extends StackableActor {

  /** Path to the `SettlementService` (could be local or remote). */
  def settlementServicePath: String

  /** Duration that an auction actor should wait for `SettlementService` to be available before shutting down. */
  def settlementServiceTimeout: Duration

  /** Attempt to identify the location of the `SettlementService` (which could be local or remote!). */
  override def receive: Receive = {
    case ActorIdentity("settlementService", Some(actorRef)) =>
      context.watch(actorRef)
      settlementService = Some(actorRef)
      context.setReceiveTimeout(Duration.Undefined)  // in case setReceiveTimeout has been previously called!
    case ActorIdentity("settlementService", None) =>
      context.setReceiveTimeout(settlementServiceTimeout)
      identify(settlementServicePath, "settlementService")
    case Terminated(actorRef) if settlementService.contains(actorRef) =>
      context.unwatch(actorRef)
      settlementService = None
      context.setReceiveTimeout(settlementServiceTimeout)
      identify(settlementServicePath, "settlementService")
    case ReceiveTimeout =>
      context.stop(self)
  }

  /* Reference for auction registry (initialized to `None`) */
  protected var settlementService: Option[ActorRef] = None

}
