package org.economicsl.auctions.actors

import scala.concurrent.duration.Duration


case class TestAuctionRegistryParticipant(auctionRegistryPath: String, auctionRegistryTimeout: Duration)
    extends AuctionRegistryParticipant
