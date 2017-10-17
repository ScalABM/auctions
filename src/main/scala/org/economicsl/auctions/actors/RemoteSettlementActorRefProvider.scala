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


/** Mixin trait providing functionality for connecting to a remote `SettlementActor`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait RemoteSettlementActorRefProvider
    extends StackableActor {

  def settlementServicePath: String

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    identifySettlementService(settlementServicePath)
  }

  override def receive: Receive = {
    case message @ ActorIdentity("settlementService", maybeActorRef) =>
      maybeActorRef match {
        case Some(actorRef) =>
          context.watch(actorRef)
          settlementService = Some(actorRef)
        case None =>
          ???  // todo what should happen in this case? Log as a warning? Then retry? Could be that settlement actor has not yet started?
      }
      super.receive(message)
    case Terminated(actorRef) if settlementService.contains(actorRef) =>
      context.unwatch(actorRef)
      identifySettlementService(settlementServicePath)
      // todo probably also want to log this as a warning!
    case message =>
      super.receive(message)
  }

  /* By default we initialize this value to `None`... */
  protected var settlementService: Option[ActorRef] = None

  protected def identifySettlementService(path: String): Unit = {
    val actorSelection = context.actorSelection(path)
    actorSelection ! Identify("settlementService")
  }

}
