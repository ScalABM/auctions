package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.OpenAuctionLike
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, BidPriceQuoting, SpreadQuoting, SpreadQuotingPolicy}
import org.economicsl.auctions.singleunit.reverse.OpenReverseAuctionLike


trait OpenDoubleAuctionLike[T <: Tradable, A] extends OpenAuctionLike[T, A] with OpenReverseAuctionLike[T, A]
  with AskPriceQuoting[T, A] with BidPriceQuoting[T, A] with SpreadQuoting[T, A] {

  protected val spreadQuotingPolicy: SpreadQuotingPolicy[T] = new SpreadQuotingPolicy[T]

}
