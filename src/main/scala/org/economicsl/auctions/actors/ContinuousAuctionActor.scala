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
import org.economicsl.auctions.AuctionProtocol
import org.economicsl.auctions.singleunit.{Auction, SealedBidAuction}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.Tradable


trait ContinuousAuctionActor[T <: Tradable, A <: Auction[T, A]]
  extends AuctionActor[T, A]
  with BidderActivityClearingSchedule[T, A]


object ContinuousAuctionActor {

  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (pricingPolicy: PricingPolicy[T],
                                       protocol: AuctionProtocol[T],
                                       settlementService: ActorRef)
                                      : Props = {
    val auction = SealedBidAuction.withDiscriminatoryClearingPolicy(pricingPolicy, protocol)
    Props(new ContinuousAuctionActorImpl(auction, Some(settlementService)))
  }

  def withUniformClearingPolicy[T <: Tradable]
                               (pricingPolicy: PricingPolicy[T],
                                protocol: AuctionProtocol[T],
                                settlementService: ActorRef)
                               : Props = {
    val auction = SealedBidAuction.withUniformClearingPolicy(pricingPolicy, protocol)
    Props(new ContinuousAuctionActorImpl(auction, Some(settlementService)))
  }


  private class ContinuousAuctionActorImpl[T <: Tradable](
    var auction: SealedBidAuction[T],
    val settlementService: Option[ActorRef])
      extends ContinuousAuctionActor[T, SealedBidAuction[T]]

}
