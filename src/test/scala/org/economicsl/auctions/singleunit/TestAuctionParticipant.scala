package org.economicsl.auctions.singleunit
import org.economicsl.auctions.{Issuer, Reference, Token}
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


case class TestAuctionParticipant(issuer: Issuer)
    extends AuctionParticipant {

  import AuctionParticipant._

  def trackOrders: PartialFunction[Any, Unit] = {
    case Accepted(_, token, order, reference) =>
      outstandingOrders += (token -> (reference -> order))
    case Canceled(_, token) =>
      outstandingOrders -= token
    case Rejected(_, _, _, _) =>
      ???
  }


  protected var outstandingOrders: Map[Token, (Reference, Order[_ <: Tradable])] = Map.empty

}
