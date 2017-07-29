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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/** Mixin trait used to schedule the issuing of orders. */
trait OrderIssuingSchedule
    extends StackableActor


object OrderIssuingSchedule {

  case object IssueOrder

}


trait RandomOrderIssuingSchedule
    extends OrderIssuingSchedule {

  import context.dispatcher  // execution context used to schedule order issuance
  import OrderIssuingSchedule._

  def delay: FiniteDuration

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    scheduleOrderIssuance(delay)
  }

  override def receive: Receive = {
    case IssueOrder =>
      scheduleOrderIssuance(delay)
      super.receive(IssueOrder)
    case message =>
      super.receive(message)
  }

  /** Schedule this `Actor` to receive an `IssueOrder` message after some delay. */
  protected def scheduleOrderIssuance(delay: FiniteDuration)(implicit ec: ExecutionContext): Unit = {
    context.system.scheduler.scheduleOnce(delay, self, IssueOrder)(ec)
  }

}



