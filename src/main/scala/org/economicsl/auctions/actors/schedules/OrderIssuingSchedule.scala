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
import org.economicsl.auctions.messages.InsertOrder
import org.economicsl.auctions.{AuctionParticipant, AuctionProtocol}
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait used to schedule the issuing of orders. */
trait OrderIssuingSchedule[P <: AuctionParticipant[P]]
    extends StackableActor {
  this: AuctionParticipantActor[P] =>

  protected case class IssueOrder[+T <: Tradable](protocol: AuctionProtocol[T])

}


/** Mixin trait used to periodically schedule orders to be issued.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PeriodicOrderIssuingSchedule[P <: AuctionParticipant[P]]
    extends OrderIssuingSchedule[P] {
  this: AuctionParticipantActor[P] =>

  def delay: FiniteDuration

  def executionContext: ExecutionContext

  override def receive: Receive = {
    case message @ IssueOrder(protocol) =>
      val issuedOrder = participant.issueOrder(protocol)
      issuedOrder match {
        case Some((updated, (orderId, order))) =>
          participant = updated  // SIDE EFFECT!!
          val senderId = participant.issuer
          val insertOrder = InsertOrder(order, orderId, senderId, currentTimeMillis())
          auctionActorRefsByTradable.get(protocol.tradable).foreach(auction => auction ! insertOrder)
        case None =>
          // if no order is issued then there should be nothing to do!
      }
      scheduleOrderIssuance(delay, protocol, executionContext)
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /** Schedule this `Actor` to receive an `IssueOrder` message after some delay. */
  protected def scheduleOrderIssuance[T <: Tradable]
                                     (delay: FiniteDuration, protocol: AuctionProtocol[T], ec: ExecutionContext)
                                     : Unit = {
    context.system.scheduler.scheduleOnce(delay, self, IssueOrder(protocol))(ec)
  }

}


/** Mixin trait for scheduling order issuing via some random process.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait RandomOrderIssuingSchedule[P <: AuctionParticipant[P]]
    extends PeriodicOrderIssuingSchedule[P] {
  this: AuctionParticipantActor[P] =>
}


/** Mixin trait for scheduling order issuing via a Poisson process.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PoissonOrderIssuingSchedule[P <: AuctionParticipant[P]]
    extends RandomOrderIssuingSchedule[P]
    with PoissonProcess {
  this: AuctionParticipantActor[P] =>
}



