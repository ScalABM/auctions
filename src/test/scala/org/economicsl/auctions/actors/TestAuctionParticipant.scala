package org.economicsl.auctions.actors

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props}
import org.economicsl.core.{Price, Tradable}

import scala.concurrent.duration.Duration


class TestAuctionParticipant private(issuer: UUID, auctionRegistryTimeout: Duration)
    extends AuctionParticipant
    with TokenProvider {


}


object TestAuctionParticipant {

  case object GenerateOrder

  def props(issuer: UUID, ): Props = {
    Props(new TestAuctionParticipant(issuer))
  }

}