package org.economicsl.auctions.singleunit
import org.economicsl.auctions.{Issuer, Reference, Token}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.core.Tradable

import scala.collection.immutable.HashMap


class TestAuctionParticipant private(
  val issuer: Issuer,
  protected val outstandingOrders: Map[Token, (Reference, Order[_ <: Tradable])])
    extends AuctionParticipant {

  import AuctionParticipant._

  def randomAskOrder[T <: Tradable](tradable: T): (Token, AskOrder[T]) = {
    ???
  }

  def randomBidOrder[T <: Tradable](tradable: T): (Token, BidOrder[T]) = {
    ???
  }

  def trackOrders(response: Either[Rejected, Accepted]): TestAuctionParticipant = response match {
    case Left(_) =>
      this  // not sure what default behavior should be when response indicates order was rejected...
    case Right(accepted) =>
      val updatedOutstandingOrders = outstandingOrders + (accepted.token -> (accepted.reference -> accepted.order))
      new TestAuctionParticipant(issuer, updatedOutstandingOrders)
  }

  def trackOrders(canceled: Canceled): TestAuctionParticipant = {
    val updatedOutstandingOrders = outstandingOrders - canceled.token
    new TestAuctionParticipant(issuer, updatedOutstandingOrders)
  }

}


object TestAuctionParticipant {

  def apply(issuer: Issuer): TestAuctionParticipant = {
    val emptyOutstandingOrders = HashMap.empty[Token, (Reference, Order[_ <: Tradable])]
    new TestAuctionParticipant(issuer, emptyOutstandingOrders)
  }

}
