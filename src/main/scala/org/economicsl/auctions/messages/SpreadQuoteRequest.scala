package org.economicsl.auctions.messages

import java.util.UUID

import org.economicsl.auctions.Issuer
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Used by auction participants to request the current spread quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class SpreadQuoteRequest[T <: Tradable](issuer: Issuer, mDReqId: UUID, timestamp: Timestamp)
  extends AuctionDataRequest[T] {

  val query: (OpenBidAuction[T]) => SpreadQuote = {
    auction => SpreadQuote(auction.orderBook.spread)
  }

}
