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

import akka.actor.{ActorRef, ReceiveTimeout}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction clearing events. */
sealed trait ClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends StackableActor {
  this: AuctionActor[T, A] =>

  /** ActorRef for the settlement service.
    *
    * @note in a remote context one might need to create an `AuctionActor` without knowing the location of the
    *       `SettlementServiceActor`. Use of `Option[ActorRef]` as type allows user to initialize this field to `None`.
    */
  def settlementService: Option[ActorRef]

}


/** Companion object for `ClearingSchedule` class. */
object ClearingSchedule {

  case object ClearRequest

}


/** Schedules a clearing event to occur whenever a new order is inserted into the auction. */
trait BidderActivityClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  import AuctionActor._

  override def receive: Receive = {
    case message @ InsertOrder(_, _: SingleUnitOrder[T]) =>
      settlementService match {
        case Some(actorRef) =>
          val (clearedAuction, contracts) = auction.clear
          contracts.foreach(contract => actorRef ! contract)  // eager eval of stream!
          auction = clearedAuction
        case None =>
          ???  // todo how to handle this case?
        // Can only occur in remote context where AuctionActor might need to be created without knowledge of the
        // location of the SettlementActor (and hence without knowledge of the ActorRef).
      }
      super.receive(message)
    case message =>
      super.receive(message)
  }

}


/** Schedules a clearing event to occur whenever no new orders have been received for a specified period. */
trait BidderInActivityClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  def timeout: FiniteDuration

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    context.setReceiveTimeout(timeout)
  }

  override def receive: Receive = {
    case ReceiveTimeout =>
      settlementService match {
        case Some(actorRef) =>
          val (clearedAuction, contracts) = auction.clear
          contracts.foreach(contract => actorRef ! contract)  // eager eval of stream!
          auction = clearedAuction
        case None =>
          ??? // todo how to handle this case?
              // Can only occur in remote context where AuctionActor might need to be created without knowledge of the
              // location of the SettlementActor (and hence without knowledge of the ActorRef).
      }
      super.receive(ReceiveTimeout)
    case message =>
      super.receive(message)
  }

}


/** Schedules a clearing event in response to an external request. */
trait OnDemandClearingSchedule[T <: Tradable, A <: Auction[T, A]]
  extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  import ClearingSchedule._

  override def receive: Receive = {
    case ClearRequest =>
      settlementService match {
        case Some(actorRef) =>
          val (updatedAuction, contracts) = auction.clear
          contracts.foreach(contract => actorRef ! contract)  // eager eval of stream!
          auction = updatedAuction
        case None =>
          ??? // todo how to handle this case?
        // Can only occur in remote context where AuctionActor might need to be created without knowledge of the
        // location of the SettlementActor (and hence without knowledge of the ActorRef).
      }
      super.receive(ClearRequest)
    case message =>
      super.receive(message)
  }

}


/** Schedules a clearing event to occur after fixed time intervals. */
trait PeriodicClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  import ClearingSchedule._
  import context.dispatcher  // implicitly passed to the scheduleClear method!

  def initialDelay: FiniteDuration

  def interval: FiniteDuration

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    scheduleClear(initialDelay)
  }

  override def receive: Receive = {
    case ClearRequest =>
      settlementService match {
        case Some(actorRef) =>
          val (updatedAuction, contracts) = auction.clear
          contracts.foreach(contract => actorRef ! contract)  // eager eval of stream!
          auction = updatedAuction
          scheduleClear(interval)
        case None =>
          ??? // todo how to handle this case?
        // Can only occur in remote context where AuctionActor might need to be created without knowledge of the
        // location of the SettlementActor (and hence without knowledge of the ActorRef).
      }
      super.receive(ClearRequest)
    case message =>
      super.receive(message)
  }

  protected def scheduleClear(interval: FiniteDuration)(implicit ec: ExecutionContext): Unit = {
    context.system.scheduler.scheduleOnce(interval, self, ClearRequest)(ec)
  }

}


/** Schedules a clearing event to occur after random time intervals. */
trait RandomClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends PeriodicClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>
}
