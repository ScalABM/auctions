package org.economicsl.remote.actors

import org.economicsl.auctions.actors.StackableActor

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



