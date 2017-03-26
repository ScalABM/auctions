package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable


trait ReverseAuctionLike[T <: Tradable, A <: ReverseAuctionLike[T, A]] {

  def insert(order: LimitAskOrder[T]): A

  def remove(order: LimitAskOrder[T]): A

}
