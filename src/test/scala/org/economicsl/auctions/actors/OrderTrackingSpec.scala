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
import org.economicsl.auctions.{Reference, TestTradable}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.core.Price
import org.scalatest.{FeatureSpecLike, GivenWhenThen, Matchers}


class OrderTrackingSpec
    extends TestKit(ActorSystem("OrderTrackingSpec"))
    with FeatureSpecLike
    with GivenWhenThen
    with Matchers {

  import org.economicsl.auctions.OrderTracker._

  val tradable = TestTradable()

  def afterAll(): Unit = {
    system.terminate()
  }

  def currentTimeMillis(): Long = {
    System.currentTimeMillis()
  }

  def randomReference(): Reference = {
    UUID.randomUUID()
  }

  feature("An OrderTracker should be able to add accepted orders to its collection of outstanding orders.") {

    val orderTrackerRef = TestActorRef[TestOrderTracker](TestOrderTracker.props(UUID.randomUUID()))
    val orderTrackerActor = orderTrackerRef.underlyingActor

    scenario ("An OrderTracker receives an Accepted message from some auction.") {

      When("an OrderTracker receives an Accepted message")

      val order = LimitBidOrder(orderTrackerActor.issuer, Price(10), tradable)

      val timestamp = currentTimeMillis()
      val token = orderTrackerActor.randomToken()
      val reference = randomReference()
      orderTrackerRef ! Accepted(timestamp, token, order, reference)

      Then("the OrderTracker should add the accepted order to its collection of outstanding orders.")

      orderTrackerActor.outstandingOrders.get(token) should be(Some((reference, order)))

    }

  }

  feature("An OrderTracker should be able to remove canceled orders from its collection of outstanding orders.") {

    val orderTrackerRef = TestActorRef[TestOrderTracker](TestOrderTracker.props(UUID.randomUUID()))
    val orderTrackerActor = orderTrackerRef.underlyingActor

    scenario ("An OrderTracker receives a Canceled message from some auction.") {

      Given("that an OrderTracker has already had at least one order accepted")

      val order = LimitAskOrder(orderTrackerActor.issuer, Price(137), tradable)

      val token = orderTrackerActor.randomToken()
      val reference = randomReference()
      orderTrackerRef ! Accepted(currentTimeMillis(), token, order, reference)

      When("that OrderTracker receives a Canceled message for one of its previously accepted orders")

      val reason = "U" // stands for user cancelled?
      orderTrackerRef ! Canceled(currentTimeMillis(), token, ???, reason)

      Then("that OrderTracker should remove the canceled order from its collection of outstanding orders.")

      orderTrackerActor.outstandingOrders.get(token) should be(None)

    }

  }

}
