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


import akka.actor.ActorRef
import org.economicsl.auctions.{AuctionParticipant, AuctionProtocol}
import org.economicsl.core.Tradable


/** Base trait for all `AuctionParticipant` actors.
  *
  * @author davidrpugh
  * @since 0.2.0
  * @todo if auction registry fails for some reason while the auction participant is "active", the auction registry will
  *       need to be re-identified; during re-identification auction participant should continue to process messages
  *       received by any auctions to which it has previously registered.
  */
trait AuctionParticipantActor[A <: AuctionParticipant[A]]
    extends OrderTrackingActor[A]
    with OrderIssuingActor[A] {

  override def receive: Receive = {
    case protocol : AuctionProtocol[Tradable] =>
      auctions = auctions + (sender() -> protocol)
    case message =>
      super.receive(message)
  }

  /* An `AuctionParticipant` needs to keep track of multiple auction protocols. */
  protected var auctions: Map[ActorRef, AuctionProtocol[Tradable]]

  protected var auctionParticipant: A

}