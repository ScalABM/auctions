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

import org.economicsl.auctions.AuctionParticipant
import org.economicsl.auctions.messages.{Accepted, Canceled, Rejected}


/** Base trait for all `AuctionParticipantActor` implementations.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait AuctionParticipantActor[A <: AuctionParticipant[A]]
    extends StackableActor {

  /** Forward received messages to `AuctionParticipant` for processing.
    *
    * @return
    */
  override def receive: Receive = {
    case message: Accepted =>
      participant = participant.handle(message)
      super.receive(message)
    case message: Canceled =>
      participant = participant.handle(message)
      super.receive(message)
    case message: Rejected =>
      participant = participant.handle(message)
      super.receive(message)
    case message =>
      super.receive(message)
  }

  protected var participant: A

}

