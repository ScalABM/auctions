package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable


trait ClearResultLike[T <: Tradable, O <: Order[T], +A <: AuctionLike[T, O, A]] {

  def fills: Option[Stream[Fill[T]]]

  def residual: A

}

