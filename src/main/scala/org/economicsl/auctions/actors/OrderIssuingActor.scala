package org.economicsl.auctions.actors

import org.economicsl.auctions.AuctionParticipant


trait OrderIssuingActor[A <: AuctionParticipant[A]]
    extends StackableActor {
  this: AuctionParticipantActor[A] =>

}
