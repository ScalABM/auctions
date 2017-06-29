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

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.economicsl.auctions.TestStock
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy

import scala.concurrent.duration._


object ExampleApplication extends App {

  val tradable: TestStock = TestStock()

  val tradingSystem = ActorSystem("TradingSystem", ConfigFactory.load())

  val auctionRegistryProps: Props = Props(classOf[AuctionRegistry])
  val auctionRegistry: ActorRef = tradingSystem.actorOf(auctionRegistryProps, "auctionRegistry")
  val auctionRegistryPath: String = auctionRegistry.path.toString

  val settlementServiceProps: Props = Props(classOf[LoggingSettlementActor])
  val settlementService: ActorRef = tradingSystem.actorOf(settlementServiceProps, "settlementService")
  val settlementServicePath: String = settlementService.path.toString


  val pricingPolicy = new MidPointPricingPolicy[TestStock]()
  val tickSize: Long = 1
  val auctionProps: Props = ContinuousDoubleAuctionActor.props(
    pricingPolicy,
    tickSize,
    tradable,
    auctionRegistryTimeout = 2.seconds,
    auctionRegistryPath,
    settlementServiceTimeout = 2.seconds,
    settlementServicePath
  )
  val auctionService: ActorRef = tradingSystem.actorOf(auctionProps, "auction")

  val participants: IndexedSeq[ActorRef] = for (i <- 1 to 1000) yield {
      val issuer = UUID.randomUUID()
      val participantProps = TestAuctionParticipantActor.props(issuer, 2.seconds, auctionRegistryPath)
      tradingSystem.actorOf(participantProps, issuer.toString)
    }

}
