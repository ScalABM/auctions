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
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.core.Tradable

import scala.concurrent.duration.FiniteDuration


trait PeriodicAuctionActor[T <: Tradable, A <: Auction[T, A]]
    extends AuctionActor[T, A]
    with PeriodicClearingSchedule[T, A]


object PeriodicAuctionActor {

  def props[T <: Tradable, A <: Auction[T, A]]
           (initialDelay: FiniteDuration, interval: FiniteDuration, auction: A, settlementService: ActorRef)
           : Props = {
    Props(new PeriodicAuctionActorImpl[T, A](auction, initialDelay, interval, Some(settlementService)))
  }


  private class PeriodicAuctionActorImpl[T <: Tradable, A <: Auction[T, A]](
    protected var auction: A,
    val initialDelay: FiniteDuration,
    val interval: FiniteDuration,
    val settlementService: Option[ActorRef])
      extends PeriodicAuctionActor[T, A]

}
