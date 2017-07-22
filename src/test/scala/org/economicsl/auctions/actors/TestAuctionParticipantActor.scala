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
package org.economicsl.auctions.actors

import akka.actor.{ActorRef, Props}
import org.economicsl.auctions.Issuer
import org.economicsl.auctions.participants.TestAuctionParticipant
import org.economicsl.core.Tradable


/**
  *
  * @param auctionParticipant
  * @note there is a coupling between AuctionParticipant auctions field which maps a Tradable to its AuctionProtocol and
  *       the auctions field of the AuctionParticipantActor which maps the a Tradable to the ActorRef for the
  *       corresponding AuctionActor.
  */
class TestAuctionParticipantActor private(var auctionParticipant: TestAuctionParticipant)
    extends AuctionParticipantActor[TestAuctionParticipant] {

  protected var auctions: Map[Tradable, ActorRef] = Map.empty[Tradable, ActorRef]

}


object TestAuctionParticipantActor {

  def props(issuer: Issuer): Props = {
    val auctionParticipant = TestAuctionParticipant(issuer)
    Props(new TestAuctionParticipantActor(auctionParticipant))
  }

}
