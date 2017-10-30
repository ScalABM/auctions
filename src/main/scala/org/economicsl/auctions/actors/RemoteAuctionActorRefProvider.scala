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

import akka.actor.{ActorIdentity, ActorRef, Identify, Terminated}
import org.economicsl.auctions.messages.NewRegistration


/** Mixin trait providing functionality for connecting to a remote `AuctionActor`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait RemoteAuctionActorRefProvider
  extends StackableActor {

  def auctionServicePath: String

  override def receive: Receive = {
    case message @ ActorIdentity("auctionService", maybeActorRef) =>
      maybeActorRef match {
        case Some(actorRef) =>
          context.watch(actorRef)
          auctionService = Some(actorRef)
          auctionService.foreach(auctionRef => auctionRef ! NewRegistration(???))  // todo should this be left to the user to specify!
        case None =>
          ???  // todo what should happen in this case? Log as a warning? Then retry? Could be that auction actor has not yet started?
      }
      super.receive(message)
    case Terminated(actorRef) if auctionService.contains(actorRef) =>
      context.unwatch(actorRef)
      identifyAuctionService(auctionServicePath)
    // todo probably also want to log this as a warning!
    case message =>
      super.receive(message)
  }

  protected var auctionService: Option[ActorRef] = None

  protected def identifyAuctionService(path: String): Unit = {
    val actorSelection = context.actorSelection(path)
    actorSelection ! Identify("auctionService")
  }

}
