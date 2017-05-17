package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, AskPriceQuotingPolicy}


trait OpenReverseAuctionLike[T <: Tradable, A] extends ReverseAuctionLike[T, A] with AskPriceQuoting[T, A] {

  protected val askPriceQuotingPolicy: AskPriceQuotingPolicy[T] = new AskPriceQuotingPolicy[T]

}
