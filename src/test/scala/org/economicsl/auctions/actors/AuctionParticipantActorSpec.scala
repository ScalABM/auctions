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
import org.economicsl.auctions.OrderTracker.{Accepted, CanceledByIssuer}
import org.economicsl.auctions.{ReferenceGenerator, TestTradable, TokenGenerator}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.core.Price
import org.economicsl.core.util.Timestamper
import org.scalatest.{FeatureSpecLike, GivenWhenThen, Matchers}


class AuctionParticipantActorSpec
    extends TestKit(ActorSystem("AuctionParticipantActorSpec"))
    with FeatureSpecLike
    with GivenWhenThen
    with Matchers
    with ReferenceGenerator
    with Timestamper
    with TokenGenerator {

  val tradable = TestTradable()

  def afterAll(): Unit = {
    system.terminate()
  }

  feature("An AuctionParticipantActor should be able to add accepted orders to its collection of outstanding orders.") {

    val issuer = UUID.randomUUID()
    val props = TestAuctionParticipantActor.props(issuer)
    val auctionParticipantActorRef = TestActorRef[TestAuctionParticipantActor](props)
    val auctionParticipantActor = auctionParticipantActorRef.underlyingActor

    scenario ("An AuctionParticipantActor receives an Accepted message from some auction.") {

      When("an AuctionParticipantActor receives an Accepted message")

      val token = randomToken()
      val order = LimitBidOrder(issuer, Price(10), tradable)

      val timestamp = currentTimeMillis()
      val reference = randomReference()
      auctionParticipantActorRef ! Accepted(timestamp, token, order, reference)

      Then("the AuctionParticipantActor should add the accepted order to its collection of outstanding orders.")

      auctionParticipantActor.auctionParticipant.outstandingOrders.get(token) should be(Some((reference, order)))

    }

  }

  feature("An AuctionParticipantActor should be able to remove canceled orders from its collection of outstanding orders.") {

    val issuer = UUID.randomUUID()
    val props = TestAuctionParticipantActor.props(issuer)
    val auctionParticipantActorRef = TestActorRef[TestAuctionParticipantActor](props)
    val auctionParticipantActor = auctionParticipantActorRef.underlyingActor

    scenario ("An AuctionParticipantActor receives a Canceled message from some auction.") {

      Given("that an AuctionParticipantActor has already had at least one order accepted")

      val token = randomToken()
      val order = LimitAskOrder(issuer, Price(137), tradable)

      val timestamp = currentTimeMillis()
      val reference = randomReference()
      auctionParticipantActorRef ! Accepted(timestamp, token, order, reference)

      When("that AuctionParticipantActor receives a Canceled message for one of its previously accepted orders")

      val laterTimestamp = currentTimeMillis()
      auctionParticipantActorRef ! CanceledByIssuer(laterTimestamp, token, order)

      Then("that AuctionParticipantActor should remove the canceled order from its collection of outstanding orders.")

      auctionParticipantActor.auctionParticipant.outstandingOrders.get(token) should be(None)

    }

  }

}
