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

import java.util.UUID

import akka.actor.{ActorRef, Terminated}
import org.economicsl.auctions.messages._
import org.economicsl.auctions.singleunit.OpenBidSingleUnitAuction
import org.economicsl.core.Tradable


/** Mixin trait that provides support for handling `AuctionDataRequest` messages.
  *
  * Basic idea is that an `AuctionParticipantActor` can send `AuctionDataRequest` messages directly in which case
  * the request is handled and the result, if any, is immediately returned to sender as part of a `AuctionDataResponse`
  * message. Alternatively, an `AuctionParticipantActor` can send a `AuctionDataSubscribe` message containing a specific
  * `AuctionDataRequest` whose query results should be returned according to some user defined schedule.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait AuctionDataPubSub[T <: Tradable]
    extends StackableActor {
  this: AuctionActor[T, OpenBidSingleUnitAuction[T]] =>

  override def receive: Receive = {
    case message: AuctionDataSubscribe[T] =>
      subscriptions = subscriptions + (message.mDReqId -> (sender() -> message.request))
      mDReqIdsByActorRef.get(sender()) match {
        case Some(mDReqIds) =>
          val updatedMDReqIds = mDReqIds + message.mDReqId
          mDReqIdsByActorRef = mDReqIdsByActorRef.updated(sender(), updatedMDReqIds)
        case None =>
          mDReqIdsByActorRef = mDReqIdsByActorRef + (sender() -> Set(message.mDReqId))
          context.watch(sender())
      }
      super.receive(message)
    case message @ AuctionDataUnsubscribe(mDReqId) =>
      subscriptions = subscriptions - mDReqId
      mDReqIdsByActorRef.get(sender()) match {
        case Some(mDReqIds) =>
          val remainingMDReqIds = mDReqIds - mDReqId
          if (remainingMDReqIds.isEmpty) {
            mDReqIdsByActorRef = mDReqIdsByActorRef - sender()  // SIDE EFFECT!
            context.unwatch(sender())
          } else {
            mDReqIdsByActorRef = mDReqIdsByActorRef.updated(sender(), remainingMDReqIds)  // SIDE EFFECT!
          }
        case None =>
          ???  // could happen if sender mistakenly sends `AuctionDataUnsubscribeRequest` to wrong `AuctionActor`!
      }
      super.receive(message)
    case message @ Terminated(actorRef) =>
      mDReqIdsByActorRef.get(actorRef).foreach(mDReqIds => subscriptions = subscriptions -- mDReqIds) // SIDE EFFECT!
      mDReqIdsByActorRef = mDReqIdsByActorRef - actorRef
      context.unwatch(actorRef)
      super.receive(message)
    case message =>
      super.receive(message)
  }

  protected def publishAuctionData(): Unit = subscriptions.foreach {
    case (mDReqId, (subscriber, dataRequest)) =>
      val auctionData = dataRequest.query(auction)
      subscriber ! AuctionDataResponse(auctionData, randomUUID(), mDReqId, currentTimeMillis())
  }

  /* Subscriptions stored as a mapping between some unique identifier and a `(ActorRef, MarketDataRequest)` pair. */
  private[this] var subscriptions: Map[UUID, (ActorRef, AuctionDataRequest[T])] = Map.empty

  /* Subscriptions stored as a mapping between `ActorRef` and its collection of subscription identifiers. */
  private[this] var mDReqIdsByActorRef: Map[ActorRef, Set[UUID]] = Map.empty

}
