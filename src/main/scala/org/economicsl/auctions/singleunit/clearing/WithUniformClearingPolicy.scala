package org.economicsl.auctions.singleunit.clearing

import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.core.Tradable


trait WithUniformClearingPolicy[T <: Tradable, A <: Auction[T]]
    extends AuctionLike[T, A]
    with UniformClearingPolicy[T, A]
