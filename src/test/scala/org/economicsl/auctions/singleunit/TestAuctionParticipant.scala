package org.economicsl.auctions.singleunit

import org.economicsl.auctions.OrderTracker.Rejected
import org.economicsl.auctions._


class TestAuctionParticipant private(val issuer: Issuer, val outstandingOrders: Map[Token, (Reference, Contract)])
    extends AuctionParticipant[TestAuctionParticipant] {

  def trackOrders(rejected: Rejected): TestAuctionParticipant = {
    ???
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Contract)]): TestAuctionParticipant = {
    new TestAuctionParticipant(issuer, updated)
  }

}


object TestAuctionParticipant {

  def withNoOutstandingOrders(issuer: Issuer): TestAuctionParticipant = {
    val noOutstandingOrders = Map.empty[Token, (Reference, Contract)]
    new TestAuctionParticipant(issuer, noOutstandingOrders)
  }

}
