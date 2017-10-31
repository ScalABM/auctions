package org.economicsl.auctions

import org.economicsl.core.Tradable


sealed trait Reason {

  def message: String

}

final case class IssuerRequestedCancel(order: Order[Tradable])
  extends Reason {
  val message: String = s"Issuer ${order.issuer} requested cancel."
}


final case class InvalidTickSize[+T <: Tradable](order: Order[T] with SinglePricePoint[T], protocol: AuctionProtocol[T])
  extends Reason {
  val message: String = s"Limit price of ${order.limit} is not a multiple of the tick size ${protocol.tickSize}."
}


final case class InvalidTradable[+T <: Tradable](order: Order[T], protocol: AuctionProtocol[T])
  extends Reason {
  val message: String = s"Order tradable ${order.tradable} must be the same as auction ${protocol.tradable}."
}
