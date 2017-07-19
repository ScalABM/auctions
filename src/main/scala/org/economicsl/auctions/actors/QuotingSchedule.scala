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

import akka.actor.ReceiveTimeout
import org.economicsl.auctions.quotes.QuoteRequest
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait that specifies a schedule for auction quoting events. */
sealed trait QuotingSchedule[T <: Tradable]
    extends StackableActor {
  this: AuctionActor[T, OpenBidAuction[T]] =>
}


/** Schedules a quoting event to occur whenever a new order is inserted into the auction. */
trait BidderActivityQuotingSchedule[T <: Tradable]
    extends QuotingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] =>

  import AuctionActor._

  override def receive: Receive = {
    case message @ InsertOrder(_, _: Order[T]) =>
      super.receive(message)  // inserts order and updates auction!
      ???
    case message =>
      super.receive(message)
  }

}


/** Schedules a clearing event to occur whenever no new orders have been received for a specified period. */
trait BidderInActivityQuotingSchedule[T <: Tradable]
    extends QuotingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] =>

  def timeout: FiniteDuration

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    context.setReceiveTimeout(timeout)
  }

  override def receive: Receive = {
    case ReceiveTimeout =>
      ???
    case message =>
      super.receive(message)
  }

}


/** Schedules a clearing event to occur after fixed time intervals. */
trait PeriodicQuotingSchedule[T <: Tradable]
    extends QuotingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] =>

  import context.dispatcher  // implicitly passed to the scheduleQuoteRequest method!

  def initialDelay: FiniteDuration

  def interval: FiniteDuration

  override def receive: Receive = {
    case request: QuoteRequest[T] =>
      val quote = auction.receive(request)
      ticker.route(quote, self)
      scheduleQuoteRequest(interval, request)
    case message =>
      super.receive(message)
  }

  protected def scheduleQuoteRequest(interval: FiniteDuration, request: QuoteRequest[T])
                                    (implicit ec: ExecutionContext)
                                    : Unit = {
    context.system.scheduler.scheduleOnce(interval, self, request)(ec)
  }

}


/** Schedules a quoting event to occur after random time intervals. */
trait RandomQuotingSchedule[T <: Tradable]
    extends PeriodicQuotingSchedule[T] {
  this: AuctionActor[T, OpenBidAuction[T]] =>
}
