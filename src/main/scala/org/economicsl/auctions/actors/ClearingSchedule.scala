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

import akka.actor.{Actor, ActorRef}
import org.economicsl.core.Tradable

import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction clearing events. */
sealed trait ClearingSchedule {
  this: Actor =>

  def clear: Receive

}


/** Companion object for `ClearingSchedule` class. */
object ClearingSchedule {

  case object ClearRequest

}


/** Schedules a clearing event to occur whenever a new order is inserted into the auction. */
trait BidderActivity[T <: Tradable, A <: { def clear: ClearResult[A] }]
    extends ClearingSchedule {
  this: Actor =>

  def settlementService: ActorRef

  def clear: Receive = {
    case InsertOrder(token, order) =>
      val results = auction.clear
      results.contracts.foreach(contract => settlementService ! contract)
      auction = results.residual
  }

  protected var auction: A

}


/** Schedules a clearing event to occur whenever no new orders have been received for a specified period. */
trait BidderInActivity extends ClearingSchedule {
  this: Actor =>
}


/** Schedules a clearing event to occur after fixed time intervals. */
trait PeriodicClearingSchedule[A <: { def clear: ClearResult[A] }]
    extends ClearingSchedule {
  this: Actor =>

  import ClearingSchedule._

  def initialDelay: FiniteDuration

  def interval: FiniteDuration

  def settlementService: ActorRef

  def handleClearRequest: Receive = {
    case ClearRequest =>
      val results = auction.clear
      results.contracts.foreach(fills => settlementService ! fills)
      auction = results.residual
  }

  protected var auction: A

}


/** Schedules a clearing event to occur after random time intervals. */
trait RandomClearingSchedule[A <: { def clear: ClearResult[A] }]
  extends PeriodicClearingSchedule[A] {
  this: StackableActor =>
}
