package org.economicsl.auctions.actors

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.Props
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}
import org.economicsl.core.{Price, Tradable}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Random


class TestAuctionParticipantActor private(val issuer: UUID, val auctionRegistryTimeout: Duration, val auctionRegistryPath: String)
    extends AuctionParticipantActor
    with TokenProvider {

  import ContinuousAuctionActor._
  import TestAuctionParticipantActor._

  @scala.throws[Exception](classOf[Exception]) override
  def preStart(): Unit = {
    super.preStart()
    context.system.scheduler.scheduleOnce(delay(), self, GenerateOrder)(context.system.dispatcher)
  }

  def generateOrder: Receive = {
    case GenerateOrder =>
      Random.shuffle(auctions).headOption.foreach{
        case (auctionRef, protocol) => auctionRef ! randomOrder(protocol.tradable)
      }
      context.system.scheduler.scheduleOnce(delay(), self, GenerateOrder)(context.system.dispatcher)
  }

  override def active: Receive = {
    super.active orElse generateOrder
  }

  private[this] def delay(): FiniteDuration = {
    val length = 1 + Random.nextInt(1000)
    FiniteDuration(length, TimeUnit.MILLISECONDS)
  }

  private[this] def randomOrder[T <: Tradable](tradable: T): InsertOrder[T] = {
    val limit = Price(Random.nextInt(Int.MaxValue))
    if (Random.nextDouble() <= 0.5) {
      InsertOrder(randomUUID(), LimitAskOrder(issuer, limit, tradable))
    } else {
      InsertOrder(randomUUID(), LimitBidOrder(issuer, limit, tradable))
    }
  }

}


object TestAuctionParticipantActor {

  def props(issuer: UUID, auctionRegistryTimeout: Duration, registrationServicePath: String): Props = {
    Props(new TestAuctionParticipantActor(issuer, auctionRegistryTimeout, registrationServicePath))
  }

  final case object GenerateOrder

}
