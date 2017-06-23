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

import akka.actor.{Actor, ActorRef, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}


/** Registry service that allows auction services and auction participants to find one another.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
class AuctionRegistry
    extends Actor {

  import AuctionRegistry._

  def receive: Receive = {
    case message @ DeregisterAuction(auctionRef) =>
      context.unwatch(auctionRef)
      auctions = auctions - auctionRef
      participants.route(message, self)  // inform all auction participants that an auction has been de-registered!
    case message @ RegisterAuction(auctionRefs) =>
      auctionRefs.foreach(context.watch)
      auctions = auctions ++ auctionRefs
      participants.route(message, self)  // inform all auction participants of the existence of new auctions!
    case DeregisterAuctionParticipant(actorRef) =>
      context.unwatch(actorRef)
      participants = participants.removeRoutee(actorRef)
    case RegisterAuctionParticipant(actorRef) =>
      context.watch(actorRef)
      participants = participants.addRoutee(actorRef)
      actorRef ! RegisterAuction(auctions)
    case Terminated(actorRef) =>
      context.unwatch(actorRef)
      if (auctions.contains(actorRef)) {
        auctions = auctions - actorRef
      } else {
        participants = participants.removeRoutee(actorRef)
      }
  }

  private[this] var auctions = Set.empty[AuctionRef]

  private[this] var participants = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee])

}


object AuctionRegistry {

  final case class DeregisterAuction(auction: AuctionRef)

  final case class DeregisterAuctionParticipant(participant: ActorRef)

  final case class RegisterAuction(auctions: Set[AuctionRef])

  object RegisterAuction {

    def apply(auction: AuctionRef): RegisterAuction = {
      RegisterAuction(Set(auction))
    }

  }

  final case class RegisterAuctionParticipant(participant: ActorRef)

}
