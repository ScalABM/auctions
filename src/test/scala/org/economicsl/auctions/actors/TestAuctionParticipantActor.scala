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
import org.economicsl.auctions.singleunit.TestSingleUnitAuctionParticipant
import org.economicsl.auctions.{AuctionProtocol, Issuer}
import org.economicsl.auctions.singleunit.participants.SingleUnitAuctionParticipant
import org.economicsl.core.{Price, Tradable}

import scala.util.Random


class TestAuctionParticipantActor(var participant: SingleUnitAuctionParticipant)
    extends AuctionParticipantActor[SingleUnitAuctionParticipant] {

  /** Maps various auction protocols to their corresponding actor refs. */
  protected var auctions: Map[AuctionProtocol[Tradable], ActorRef] = Map.empty[AuctionProtocol[Tradable], ActorRef]

}


object TestAuctionParticipantActor {

  def props(prng: Random, askOrderProbability: Double, issuer: Issuer, valuations: Map[Tradable, Price]): Props = {
    val participant = TestSingleUnitAuctionParticipant.withNoOutstandingOrders(prng, askOrderProbability, issuer, valuations)
    Props(new TestAuctionParticipantActor(participant))
  }

}
