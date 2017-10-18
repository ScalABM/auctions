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

import org.economicsl.auctions.{AuctionParticipant, AuctionProtocol}
import org.economicsl.auctions.actors.{AuctionParticipantActor, StackableActor}
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction data requests.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait AuctionDataRequestSchedule[P <: AuctionParticipant[P]]
    extends StackableActor {
  this: AuctionParticipantActor[P] =>

  protected case class RequestAuctionData[+T <: Tradable](protocol: AuctionProtocol[T])

}


/** Schedules an `AuctionDataRequest` to be sent after fixed time intervals.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PeriodicAuctionDataRequestSchedule[P <: AuctionParticipant[P]]
    extends AuctionDataRequestSchedule[P] {
  this: AuctionParticipantActor[P] =>

  def executionContext: ExecutionContext

  def delay: FiniteDuration

  override def receive: Receive = {
    case message @ RequestAuctionData(protocol) =>
      val requestedAuctionData = participant.requestAuctionData(protocol)
      requestedAuctionData match {
        case Some((updated, (token, auctionDataRequest))) =>
          participant = updated  // SIDE EFFECT!!
          auctionActorRefsByTradable.get(protocol.tradable).foreach(auction => auction ! auctionDataRequest)
        case None =>
          // if no `AuctionDataRequest` is issued then there should be nothing to do!
      }
      scheduleRequestAuctionData(delay, protocol, executionContext)
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /** Schedule this `Actor` to receive a `RequestAuctionData` message after some delay. */
  protected def scheduleRequestAuctionData[T <: Tradable]
                                          (delay: FiniteDuration, protocol: AuctionProtocol[T], ec: ExecutionContext)
                                          : Unit = {
    context.system.scheduler.scheduleOnce(delay, self, RequestAuctionData(protocol))(ec)
  }

}


/** Base trait for all `RandomAuctionDataRequestSchedule` implementations.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait RandomAuctionDataRequestSchedule[P <: AuctionParticipant[P]]
    extends PeriodicAuctionDataRequestSchedule[P] {
  this: AuctionParticipantActor[P] =>
}


/** Mixin trait for scheduling request for auction data as a Poisson process.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait PoissonAuctionDataRequestSchedule[P <: AuctionParticipant[P]]
    extends RandomAuctionDataRequestSchedule[P]
    with PoissonProcess {
  this: AuctionParticipantActor[P] =>
}

