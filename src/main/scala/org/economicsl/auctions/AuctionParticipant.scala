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
package org.economicsl.auctions

import org.economicsl.core.Tradable


trait AuctionParticipant[A <: AuctionParticipant[A]]
    extends OrderIssuer[A]
    with OrderTracker[A] {
  this: A =>

  import Auction._

  def + (protocol: AuctionProtocol): A = {
    val updatedAuctions = auctions + (protocol.tradable -> protocol)
    withAuctions(updatedAuctions)
  }

  def - (tradable: Tradable): A = {
    val updatedAuctions = auctions - tradable
    withAuctions(updatedAuctions)
  }

  protected def withAuctions(updated: Map[Tradable, AuctionProtocol]): A

  /** Each `AuctionParticipant` should maintain a collection of auctions in which it actively participates. */
  protected def auctions: Map[Tradable, AuctionProtocol]

}

