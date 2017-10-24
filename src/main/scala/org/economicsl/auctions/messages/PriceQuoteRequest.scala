/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.messages

import java.util.UUID

import org.economicsl.auctions.Issuer
import org.economicsl.auctions.singleunit.OpenBidAuction
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Base trait for all price quote requests.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuoteRequest[T <: Tradable] extends AuctionDataRequest[T]


/** Used by auction participants to request the current ask price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class AskPriceQuoteRequest[T <: Tradable](issuer: Issuer, mDReqId: UUID, timestamp: Timestamp)
  extends PriceQuoteRequest[T] {

  val query: (OpenBidAuction[T]) => AskPriceQuote[T] = {
    auction => AskPriceQuote(auction.protocol.tradable, auction.orderBook.askPriceQuote)
  }

}


/** Used by auction participants to request the current bid price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class BidPriceQuoteRequest[T <: Tradable](issuer: Issuer, mDReqId: UUID, timestamp: Timestamp)
  extends PriceQuoteRequest[T] {

  val query: (OpenBidAuction[T]) => BidPriceQuote[T] = {
    auction => BidPriceQuote(auction.protocol.tradable, auction.orderBook.bidPriceQuote)
  }

}


/** Used by auction participants to request the current mid-point price quote.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
final case class MidPointPriceQuoteRequest[T <: Tradable](issuer: Issuer, mDReqId: UUID, timestamp: Timestamp)
  extends PriceQuoteRequest[T] {

  val query: (OpenBidAuction[T]) => MidPointPriceQuote[T] = {
    auction => MidPointPriceQuote(auction.protocol.tradable, auction.orderBook.midPointPriceQuote)
  }

}