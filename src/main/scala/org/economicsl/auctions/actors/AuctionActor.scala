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

import akka.actor.{ActorRef, Props}
import org.economicsl.auctions.actors.schedules.{BidderActivityClearingSchedule, ClearingSchedule, PeriodicClearingSchedule}
import org.economicsl.auctions.messages.{CancelOrder, InsertOrder}
import org.economicsl.auctions.singleunit.{Auction, SealedBidAuction}
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable

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
    extends StackableActor {
  this: ClearingSchedule[T, A] =>

  import AuctionActor._

  override def receive: Receive = {
    case message @ InsertOrder(_, token, order: SingleUnitOrder[T]) =>
      val (updatedAuction, response) = auction.insert(token -> order)
      response match {
        case Right(accepted) =>
          sender() ! accepted
          auction = updatedAuction
        case Left(rejected) =>
          sender() ! rejected
      }
      super.receive(message)
    case message @ CancelOrder(reference, _, _) =>
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
      super.receive(message)
    case message @ RegisterAuctionParticipant(participant) =>
      context.watch(participant)  // `AuctionActor` notified if `AuctionParticipantActor` "dies"...
      participant ! auction.protocol
      super.receive(message)
    case message @ DeregisterAuctionParticipant(participant) =>
      context.unwatch(participant)  // `AuctionActor` no longer be notified if `AuctionParticipantActor` "dies"...
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /* `Auction` mechanism encapsulates the relevant state. */
  protected var auction: A

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


  final case class DeregisterAuctionParticipant(participant: ActorRef)


  final case class RegisterAuctionParticipant(participant: ActorRef)


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