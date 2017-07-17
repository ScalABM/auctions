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

import akka.actor.ActorRef
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.core.Tradable

import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction clearing events. */
sealed trait ClearingSchedule[T <: Tradable, A <: Auction[T, A]] {
  this: AuctionActor[T, A] =>

  def clear: Receive

}


/** Companion object for `ClearingSchedule` class. */
object ClearingSchedule {

  case object ClearRequest

}


/** Schedules a clearing event to occur whenever a new order is inserted into the auction. */
trait BidderActivity[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  import AuctionActor._

  def clear: Receive = {
    case InsertOrder(token, order) =>
      val (updatedAuction, contracts) = auction.clear
      contracts.foreach(contract => settlementService ! contract)
      auction = updatedAuction
  }

}


/** Schedules a clearing event to occur whenever no new orders have been received for a specified period. */
trait BidderInActivity[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>
}


/** Schedules a clearing event to occur after fixed time intervals. */
trait PeriodicClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends ClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>

  import ClearingSchedule._

  def initialDelay: FiniteDuration

  def interval: FiniteDuration

  def settlementService: ActorRef

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val (updatedAuction, contracts) = auction.clear
      contracts.foreach(contract => settlementService ! contract)
      auction = updatedAuction
  }

  protected var auction: A

}


/** Schedules a clearing event to occur after random time intervals. */
trait RandomClearingSchedule[T <: Tradable, A <: Auction[T, A]]
    extends PeriodicClearingSchedule[T, A] {
  this: AuctionActor[T, A] =>
}
