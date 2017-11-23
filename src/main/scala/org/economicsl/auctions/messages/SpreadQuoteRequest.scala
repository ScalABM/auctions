package org.economicsl.auctions.messages

import java.util.UUID

import org.economicsl.auctions.IssuerId
import org.economicsl.auctions.singleunit.OpenBidSingleUnitAuction
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Used by auction participants to request the current spread quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class SpreadQuoteRequest[T <: Tradable](senderId: IssuerId, mDReqId: UUID, timestamp: Timestamp)
  extends AuctionDataRequest[T] {

  val query: (OpenBidSingleUnitAuction[T]) => SpreadQuote[T] = {
    auction => SpreadQuote(auction.protocol.tradable, auction.orderBook.spread)
  }

}
