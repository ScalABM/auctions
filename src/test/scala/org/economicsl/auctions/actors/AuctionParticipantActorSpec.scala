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

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.economicsl.auctions.messages.{Accepted, CanceledByIssuer}
import org.economicsl.auctions.{OrderReferenceIdGenerator, TestTradable, OrderIdGenerator}
import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder, SingleUnitBidOrder}
import org.economicsl.core.{Price, Tradable}
import org.economicsl.core.util.Timestamper
import org.scalatest.{FeatureSpecLike, GivenWhenThen, Matchers}

import scala.util.Random


class AuctionParticipantActorSpec
    extends TestKit(ActorSystem("AuctionParticipantActorSpec"))
    with FeatureSpecLike
    with GivenWhenThen
    with Matchers
    with OrderReferenceIdGenerator
    with Timestamper
    with OrderIdGenerator {

  val tradable = TestTradable()

  def afterAll(): Unit = {
    system.terminate()
  }

  feature("An AuctionParticipantActor should be able to add accepted orders to its collection of outstanding orders.") {

    val prng = new Random(42)
    val askOrderProbability = 0.5
    val issuer = UUID.randomUUID()
    val valuations = Map.empty[Tradable, Price]
    val props = TestAuctionParticipantActor.props(prng, askOrderProbability, issuer, valuations)
    val auctionParticipantActorRef = TestActorRef[TestAuctionParticipantActor](props)
    val auctionParticipantActor = auctionParticipantActorRef.underlyingActor

    scenario ("An AuctionParticipantActor receives an Accepted message from some auction.") {

      When("an AuctionParticipantActor receives an Accepted message")

      val token = randomOrderId()
      val order = SingleUnitBidOrder(issuer, Price(10), tradable)

      val timestamp = currentTimeMillis()
      val reference = randomOrderReferenceId()
      auctionParticipantActorRef ! Accepted(timestamp, token, order, reference)

      Then("the AuctionParticipantActor should add the accepted order to its collection of outstanding orders.")

      auctionParticipantActor.participant.outstandingOrders.get(token) should be(Some((reference, order)))

    }

  }

  feature("An AuctionParticipantActor should be able to remove canceled orders from its collection of outstanding orders.") {

    val prng = new Random(42)
    val askOrderProbability = 0.5
    val issuer = UUID.randomUUID()
    val valuations = Map.empty[Tradable, Price]
    val props = TestAuctionParticipantActor.props(prng, askOrderProbability, issuer, valuations)
    val auctionParticipantActorRef = TestActorRef[TestAuctionParticipantActor](props)
    val auctionParticipantActor = auctionParticipantActorRef.underlyingActor

    scenario ("An AuctionParticipantActor receives a Canceled message from some auction.") {

      Given("that an AuctionParticipantActor has already had at least one order accepted")

      val token = randomOrderId()
      val order = SingleUnitAskOrder(issuer, Price(137), tradable)

      val timestamp = currentTimeMillis()
      val reference = randomOrderReferenceId()
      auctionParticipantActorRef ! Accepted(timestamp, token, order, reference)

      When("that AuctionParticipantActor receives a Canceled message for one of its previously accepted orders")

      val laterTimestamp = currentTimeMillis()
      auctionParticipantActorRef ! CanceledByIssuer(laterTimestamp, token, order)

      Then("that AuctionParticipantActor should remove the canceled order from its collection of outstanding orders.")

      auctionParticipantActor.participant.outstandingOrders.get(token) should be(None)

    }

  }

}
