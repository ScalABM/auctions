/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.actors

import akka.actor.{ActorRef, Props, Terminated}
import org.economicsl.auctions.actors.schedules.{BidderActivityClearingSchedule, ClearingSchedule, PeriodicClearingSchedule}
import org.economicsl.auctions.messages._
import org.economicsl.auctions.singleunit.{Auction, SealedBidAuction}
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable
import org.economicsl.core.util.{Timestamper, UUIDGenerator}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Base trait for all `AuctionActor` implementations.
  *
  * The base `AuctionActor` encapsulates the state of the `auction` mechanism. An `AuctionActor` updates this state by
  * processing `InsertOrder` and `CancelOrder` messages received from registered `AuctionParticipantActor` instances.
  * In addition to encapsulating the `auction` mechanism state, an `AuctionActor` is also responsible for registering
  * and de-registering `AuctionParticipantActor` instances. An `AuctionActor` can communicate with the registered
  * `AuctionParticipantActor` instances by sending messages to its `ticker`. All messages received by the `ticker` are
  * broadcast to registered `AuctionParticipantActor` instances.
  * @tparam T
  * @tparam A
  */
trait AuctionActor[T <: Tradable, A <: Auction[T, A]]
    extends StackableActor
    with Timestamper
    with UUIDGenerator {
  this: ClearingSchedule[T, A] =>

  override def receive: Receive = {
    case message @ InsertOrder(order: SingleUnitOrder[T], orderId, _, _) =>
      val (updatedAuction, response) = auction.insert(orderId -> order)
      response match {
        case Right(accepted) =>
          sender() ! accepted
          auction = updatedAuction
        case Left(rejected) =>
          sender() ! rejected
          auction = updatedAuction
      }
      super.receive(message)
    case message: CancelOrder =>
      val (updatedAuction, cancelOrderResponse) = auction.cancel(message)
      cancelOrderResponse match {
        case Right(cancelOrderAccepted) =>
          sender() ! cancelOrderAccepted
          auction = updatedAuction
        case Left(cancelOrderRejected) =>
          sender() ! cancelOrderRejected
          auction = updatedAuction
      }
      super.receive(message)
    case message @ NewRegistration(registId) =>
      if (validate(message)) {
        context.watch(sender()) // `AuctionActor` notified if `AuctionParticipantActor` "dies"...
        val registRefId = randomUUID()
        participants = participants + (registRefId -> (registId -> sender()))
        sender() ! NewRegistrationAccepted(registId, registRefId)
        sender() ! auction.protocol // todo check fix protocol to see whether `AcceptedNewRegistrationInstructions` message could include auction protocol information.
      } else {
        sender() ! NewRegistrationRejected(registId)
      }
      super.receive(message)
    case message @ ReplaceRegistration(registId, registRefId) =>
      if (participants.contains(registRefId)) {
        participants = participants.updated(registRefId, registId -> sender())
        sender() ! ReplaceRegistrationAccepted(registId, registRefId)
      } else {
        sender() ! ReplaceRegistrationRejected(registId, registRefId)
      }
      super.receive(message)
    case message @ CancelRegistration(registId, registRefId) =>
      if (participants.contains(registRefId)) {
        context.unwatch(sender()) // `AuctionActor` no longer notified if `AuctionParticipantActor` "dies"...
        participants = participants - registRefId
        sender() ! CancelRegistrationAccepted(registId, registRefId)
      } else {
        sender() ! CancelRegistrationRejected(registId, registRefId)
      }
      super.receive(message)
    case message @ Terminated(actorRef) =>
      val existingParticipant = participants find { case (_, (_, existingActorRef)) => actorRef == existingActorRef }
      existingParticipant foreach {
        case (registRefId, (_ , participantActorRef) ) =>
          context.unwatch(participantActorRef)
          participants = participants - registRefId
      }
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /* `Auction` mechanism encapsulates the relevant state. */
  protected var auction: A

  private[this] var participants: Map[RegistrationReferenceId, (RegistrationId, ActorRef)] = Map.empty

  private[this] def validate(registrationInstructions: NewRegistration): Boolean = {
    true  // todo nothing to validate...yet!
  }

}


object AuctionActor {

  /** Creates `Props` for an `AuctionActor with BidderActivityClearingSchedule`.
    *
    * @param auction a `SealedBidAuction` mechanism.
    * @param settlementService the `ActorRef` for the `SettlementService`.
    * @tparam T
    * @return a `Props` instance used to create an instance of an `AuctionActor with BidderActivityClearingSchedule`.
    */
  def withBidderActivityClearingSchedule[T <: Tradable]
                                        (auction: SealedBidAuction[T], settlementService: ActorRef)
                                        : Props = {
    Props(new WithBidderActivityClearingSchedule[T](auction, Some(settlementService)))
  }

  /** Creates `Props` for an `AuctionActor with PeriodicClearingSchedule`.
    *
    * @param auction a `SealedBidAuction` mechanism.
    * @param executionContext
    * @param initialDelay a `FiniteDuration` specifying the delay between the time the `AuctionActor` is created and
    *                     the initial clearing event.
    * @param interval a `FiniteDuration` specifying the interval between clearing events.
    * @param settlementService
    * @tparam T
    * @return a `Props` instance used to create an instance of an `AuctionActor with PeriodicClearingSchedule`.
    */
  def withPeriodicClearingSchedule[T <: Tradable]
                                  (auction: SealedBidAuction[T],
                                   executionContext: ExecutionContext,
                                   initialDelay: FiniteDuration,
                                   interval: FiniteDuration,
                                   settlementService: ActorRef)
                                  : Props = {
    Props(new WithPeriodicClearingSchedule[T](auction, executionContext, initialDelay, interval, Some(settlementService)))
  }


  /** Default implementation of an `AuctionActor with BidderActivityClearingSchedule`.
    *
    * An `AuctionActor with BidderActivityClearingSchedule` is used for modeling sealed-bid continuous double
    * auctions.
    * @param auction a `SealedBidAuction` mechanism.
    * @param settlementService
    * @tparam T
    */
  private class WithBidderActivityClearingSchedule[T <: Tradable](
    protected var auction: SealedBidAuction[T],
    protected var settlementService: Option[ActorRef])
      extends AuctionActor[T, SealedBidAuction[T]]
      with BidderActivityClearingSchedule[T, SealedBidAuction[T]]


  /** Default implementation of an `AuctionActor with PeriodicClearingSchedule`.
    *
    * @param auction a `SealedBidAuction` mechanism.
    * @param executionContext
    * @param initialDelay
    * @param interval
    * @param settlementService
    * @tparam T
    */
  private class WithPeriodicClearingSchedule[T <: Tradable](
    protected var auction: SealedBidAuction[T],
    val executionContext: ExecutionContext,
    val initialDelay: FiniteDuration,
    val interval: FiniteDuration,
    protected var settlementService: Option[ActorRef])
      extends AuctionActor[T, SealedBidAuction[T]]
      with PeriodicClearingSchedule[T, SealedBidAuction[T]]

}