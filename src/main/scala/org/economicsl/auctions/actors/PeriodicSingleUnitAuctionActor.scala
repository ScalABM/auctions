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
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.auctions.singleunit.{SealedBidSingleUnitAuction, SingleUnitAuction}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}

import scala.concurrent.duration.FiniteDuration


trait PeriodicSingleUnitAuctionActor[T <: Tradable, A <: SingleUnitAuction[T, A]]
    extends SingleUnitAuctionActor[T, A]
    with PeriodicClearingSchedule[T, SingleUnitOrder[T], A]


object PeriodicSingleUnitAuctionActor {

  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (initialDelay: FiniteDuration,
                                       interval: FiniteDuration,
                                       pricingPolicy: PricingPolicy[T],
                                       settlementService: ActorRef,
                                       tickSize: Currency,
                                       tradable: T)
                                       : Props = {
    val auction = SealedBidSingleUnitAuction.withDiscriminatoryClearingPolicy(pricingPolicy, tickSize, tradable)
    Props(new PeriodicSingleUnitAuctionActorImpl(auction, initialDelay, interval, Some(settlementService)))
  }

  def withUniformClearingPolicy[T <: Tradable]
                               (initialDelay: FiniteDuration,
                                interval: FiniteDuration,
                                pricingPolicy: PricingPolicy[T],
                                settlementService: ActorRef,
                                tickSize: Currency,
                                tradable: T)
                                : Props = {
    val auction = SealedBidSingleUnitAuction.withUniformClearingPolicy(pricingPolicy, tickSize, tradable)
    Props(new PeriodicSingleUnitAuctionActorImpl(auction, initialDelay, interval, Some(settlementService)))
  }


  private class PeriodicSingleUnitAuctionActorImpl[T <: Tradable](
    var auction: SealedBidSingleUnitAuction[T],
    val initialDelay: FiniteDuration,
    val interval: FiniteDuration,
    val settlementService: Option[ActorRef])
      extends PeriodicSingleUnitAuctionActor[T, SealedBidSingleUnitAuction[T]]

}
