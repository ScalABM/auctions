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
package org.economicsl.auctions.participants

import org.economicsl.auctions.Auction.AuctionProtocol
import org.economicsl.auctions._
import org.economicsl.core.Tradable


class TestAuctionParticipant private(
  val issuer: Issuer,
  protected val auctions: Map[Tradable, AuctionProtocol],
  val outstandingOrders: Map[Token, (Reference, Contract)])
    extends AuctionParticipant[TestAuctionParticipant] {

  protected def withAuctions(updated: Map[Tradable, AuctionProtocol]): TestAuctionParticipant = {
    new TestAuctionParticipant(issuer, updated, outstandingOrders)
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Contract)]): TestAuctionParticipant = {
    new TestAuctionParticipant(issuer, auctions, updated)
  }

}


object TestAuctionParticipant {

  def apply(issuer: Issuer): TestAuctionParticipant = {
    val emptyAuctions = Map.empty[Tradable, AuctionProtocol]
    val emptyOutstandingOrders = Map.empty[Token, (Reference, Contract)]
    new TestAuctionParticipant(issuer, emptyAuctions, emptyOutstandingOrders)
  }

}
