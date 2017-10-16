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
package org.economicsl.auctions.actors.schedules

import akka.actor.ReceiveTimeout
import org.economicsl.auctions.actors.{AuctionActor, AuctionDataPubSub, StackableActor}
import org.economicsl.auctions.messages.{CancelOrder, InsertOrder, AuctionDataRequest, AuctionDataResponse}
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction data publishing events.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait AuctionDataPublishingSchedule[T <: Tradable]
    extends StackableActor {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>
}


/** Schedules publishing of auction data whenever a new order is inserted (or an existing order is cancelled).
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait BidderActivityAuctionDataPublishingSchedule[T <: Tradable]
    extends AuctionDataPublishingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>

  override def receive: Receive = {
    case message: InsertOrder[T] =>
      publishAuctionData()
      super.receive(message)
    case message: CancelOrder =>
      publishAuctionData()
      super.receive(message)
    case message =>
      super.receive(message)
  }

}


/** Schedules publishing of auction data whenever there has been no bidder activity for a specified period.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait BidderInActivityAuctionDataPublishingSchedule[T <: Tradable]
    extends AuctionDataPublishingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>

  def timeout: FiniteDuration

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    context.setReceiveTimeout(timeout)
  }

  override def receive: Receive = {
    case ReceiveTimeout =>
      publishAuctionData()
      super.receive(ReceiveTimeout)
    case message =>
      super.receive(message)
  }

}


/** Schedules publishing of auction data in response to a specific `AuctionParticipantActor` request.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait OnDemandAuctionDataPublishingSchedule[T <: Tradable]
    extends AuctionDataPublishingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>

  override def receive: Receive = {
    case message: AuctionDataRequest[A] =>
      val marketData = message.query(auction)
      sender() ! AuctionDataResponse(marketData, randomUUID(), message.mDReqId, currentTimeMillis())
      super.receive(message)
    case message =>
      super.receive(message)
  }

}


/** Schedules publishing of auction data after fixed time intervals.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait PeriodicAuctionDataPublishingSchedule[T <: Tradable]
    extends AuctionDataPublishingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>

  def executionContext: ExecutionContext

  def interval: FiniteDuration

  override def receive: Receive = {
    case PublishAuctionData =>
      publishAuctionData()
      scheduleOnce(interval, executionContext)
      super.receive(PublishAuctionData)
    case message =>
      super.receive(message)
  }

  protected def scheduleOnce(interval: FiniteDuration, ec: ExecutionContext): Unit = {
    context.system.scheduler.scheduleOnce(interval, self, PublishAuctionData)(ec)
  }

  protected case object PublishAuctionData

}


/** Schedules publishing of auction data to occur after random time intervals.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait RandomAuctionDataPublishingSchedule[T <: Tradable]
    extends PeriodicAuctionDataPublishingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>
}


/** Mixin trait for scheduling auction data publication via a Poisson process.
  *
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
trait PoissonAuctionDataPublishingSchedule[T <: Tradable]
    extends RandomAuctionDataPublishingSchedule[T]
    with PoissonProcess {
  this: AuctionActor[T, OpenBidAuction[T]] with AuctionDataPubSub[T, OpenBidAuction[T]] =>
}
