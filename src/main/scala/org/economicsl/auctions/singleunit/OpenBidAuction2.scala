package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.ClearingPolicy
import org.economicsl.core.Tradable


trait OpenBidAuction2[T <: Tradable, A <: OpenBidAuction2[T, A]]
  extends Auction2[T, A] {
  this: A with ClearingPolicy[T, A] with QuotingPolicy[T] =>
}
