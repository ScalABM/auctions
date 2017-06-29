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

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{FeatureSpecLike, GivenWhenThen}


class AuctionRegistryParticipantSpec
    extends TestKit(ActorSystem("AuctionRegistryParticipantSpec"))
    with FeatureSpecLike
    with GivenWhenThen {

  def afterAll(): Unit = {
    system.terminate()
  }

  val auctionRegistry = TestProbe()

  feature("AuctionRegistryParticipant should be able to identify an active AuctionRegistry.") {
    ???
  }

  feature("AuctionRegistryParticipant should stop if it is unable to identify an AuctionRegistry") {
    ???
  }

  feature("If AuctionRegistry fails, then AuctionRegistryParticipant should try to re-identify the AuctionRegistry") {
    ???
  }

}
