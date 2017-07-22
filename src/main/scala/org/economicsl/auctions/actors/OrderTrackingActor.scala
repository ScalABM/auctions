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

import org.economicsl.auctions.participants.AuctionParticipant


/** Mixin trait providing `OrderTracking` behavior.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderTrackingActor[A <: AuctionParticipant[A]]
    extends StackableActor {
  this: AuctionParticipantActor[A] =>

  import org.economicsl.auctions.participants.OrderTracker._

  /** Forward received messages to `AuctionParticipant` for processing.
    *
    * @return
    * @todo consider changing type signature of `trackOrders` can handle either `Accepted`, `Canceled` or `Rejected`.
    *       This will also require creating a common super-type for these messages.
    */
  override def receive: Receive = {
    case message: Accepted =>
      auctionParticipant = auctionParticipant.trackOrders(message)
      super.receive(message)
    case message: Canceled =>
      auctionParticipant = auctionParticipant.trackOrders(message)
      super.receive(message)
    case message: Rejected =>
      auctionParticipant = auctionParticipant.trackOrders(message)
      super.receive(message)
    case message =>
      super.receive(message)
  }

}

