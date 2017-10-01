package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.orders.SingleUnitOrder
import org.economicsl.auctions.{AuctionParticipant, AuctionProtocol, Token}
import org.economicsl.core.Tradable


trait SingleUnitAuctionParticipant
  extends AuctionParticipant[SingleUnitAuctionParticipant] {

  /** Each `AuctionParticipant` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return a `Tuple2` whose first element contains a `Token` that uniquely identifies an `Order` and whose second
    *         element is an `Order`.
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitAuctionParticipant, (Token, SingleUnitOrder[T]))]

}
