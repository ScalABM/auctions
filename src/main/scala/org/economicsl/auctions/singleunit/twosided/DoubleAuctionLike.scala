package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.{AuctionLike, Order}


trait DoubleAuctionLike[T <: Tradable, +A <: DoubleAuctionLike[T, A]] extends AuctionLike[T, Order[T], A] {

  def clear: ClearResult[T, A]

}
