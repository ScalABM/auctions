/*
Copyright 2016 David R. Pugh

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

import org.economicsl.auctions.actors.{AuctionParticipantActor, StackableActor}
import org.economicsl.auctions.messages.CancelOrder
import org.economicsl.auctions.AuctionParticipant
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait used to schedule the cancellation of orders.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderCancellationSchedule[P <: AuctionParticipant[P]]
    extends StackableActor {
  this: AuctionParticipantActor[P] =>

  /* Message used to prompt `AuctionParticipantActor` to cancel an order (INTERNAL USE ONLY)! */
  protected case object IssueOrderCancellation

}


/** Mixin trait used to periodically schedule order cancellations.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PeriodicOrderCancellationSchedule[P <: AuctionParticipant[P]]
    extends OrderCancellationSchedule[P] {
  this: AuctionParticipantActor[P] =>

  def delay: FiniteDuration

  def executionContext: ExecutionContext

  override def receive: Receive = {
    case IssueOrderCancellation =>
      val cancelledOrder = participant.outstandingOrders.headOption
      cancelledOrder.foreach {
        case (orderId, (orderRefId, order)) =>
          val senderId = participant.participantId
          val cancelOrder = CancelOrder(orderId, orderRefId, senderId, currentTimeMillis())
          val auctionActorRef = auctionActorRefsByTradable(order.tradable)
          auctionActorRef ! cancelOrder
      }
      scheduleOrderCancellation(delay, executionContext)
      super.receive(IssueOrderCancellation)
    case message =>
      super.receive(message)
  }

  /** Schedule this `AuctionParticipantActor` to receive an `IssueOrderCancellation` message after some delay. */
  protected def scheduleOrderCancellation[T <: Tradable](delay: FiniteDuration, ec: ExecutionContext): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, IssueOrderCancellation)(ec)
  }

}


/** Mixin trait for scheduling order cancellation via some random process.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait RandomOrderCancellationSchedule[P <: AuctionParticipant[P]]
    extends PeriodicOrderCancellationSchedule[P] {
  this: AuctionParticipantActor[P] =>
}


/** Mixin trait for scheduling order cancellation via a Poisson process.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PoissonOrderCancellationSchedule[P <: AuctionParticipant[P]]
    extends RandomOrderCancellationSchedule[P]
    with PoissonProcess {
  this: AuctionParticipantActor[P] =>
}



