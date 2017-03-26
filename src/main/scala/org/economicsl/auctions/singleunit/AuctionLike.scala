package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{Price, Tradable}


/** Mixin trait providing behaviors relevant for auctions. */
trait AuctionLike[T <: Tradable, A <: AuctionLike[T, A]] {

  def insert(order: LimitBidOrder[T]): A

  def remove(order: LimitBidOrder[T]): A

}
