package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.ClearingPolicy
import org.economicsl.core.Tradable


trait SealedBidAuction2[T <: Tradable, A <: SealedBidAuction2[T, A]]
    extends Auction2[T, A] {
  this: A with ClearingPolicy[T, A] =>
}
