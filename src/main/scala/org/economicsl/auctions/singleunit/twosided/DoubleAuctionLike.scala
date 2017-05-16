package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.AuctionLike
import org.economicsl.auctions.singleunit.reverse.ReverseAuctionLike


trait DoubleAuctionLike[T <: Tradable, A] extends AuctionLike[T, A] with ReverseAuctionLike[T, A]
