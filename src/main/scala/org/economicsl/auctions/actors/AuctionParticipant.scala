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


import akka.actor.ActorIdentity


/** Base trait for all `AuctionParticipant` actors.
  *
  * @author davidrpugh
  * @since 0.2.0
  * @note if auction registry fails for some reason it will need to be re-identified; during re-identification auction
  *       participant should continue to process messages received by any auctions to which it has previously registered.
  */
trait AuctionParticipant
    extends StackableActor
    with AuctionRegistryIdentifier
    with OrderTracking {

  wrappedBecome(identifyingAuctionRegistry)

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    identify(auctionRegistryPath, "auctionRegistry")
  }

  override def identifyingAuctionRegistry: Receive = {
    case message @ ActorIdentity("auctionRegistry", Some(_)) =>
      super.receive(message)
      context.become(active)
  }

  def active: Receive = {
    registeringAuctions orElse trackingOrders
  }

}
