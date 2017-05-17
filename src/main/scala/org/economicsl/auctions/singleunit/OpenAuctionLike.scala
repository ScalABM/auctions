package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}


trait OpenAuctionLike[T <: Tradable, A] extends AuctionLike[T, A] with BidPriceQuoting[T, A] {

  protected val bidPriceQuotingPolicy: BidPriceQuotingPolicy[T] = new BidPriceQuotingPolicy[T]

}
