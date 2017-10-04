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
package org.economicsl.auctions.actors.schedules

import org.economicsl.auctions.AuctionParticipant
import org.economicsl.auctions.actors.{AuctionParticipantActor, StackableActor}
import org.economicsl.auctions.quotes.QuoteRequest
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for quote requests. */
sealed trait QuoteRequestSchedule[P <: AuctionParticipant[P]]
    extends StackableActor {
  this: AuctionParticipantActor[P] =>
}


/** Schedules a quote request event to occur after fixed time intervals. */
trait PeriodicQuoteRequestSchedule[P <: AuctionParticipant[P]]
    extends QuoteRequestSchedule[P] {
  this: AuctionParticipantActor[P] =>

  def executionContext: ExecutionContext

  def interval: FiniteDuration

  override def receive: Receive = {
    ???
  }

  protected def scheduleQuoteRequest[T <: Tradable](interval: FiniteDuration, request: QuoteRequest[T], ec: ExecutionContext): Unit = {
    context.system.scheduler.scheduleOnce(interval, ???, request)(ec)
  }

}


/** Schedules a quoting event to occur after random time intervals. */
trait RandomQuoteRequestSchedule[P <: AuctionParticipant[P]]
    extends PeriodicQuoteRequestSchedule[P] {
  this: AuctionParticipantActor[P] =>
}
