package org.economicsl.auctions.multiunit

import org.economicsl.auctions.Auction
import org.economicsl.core.Tradable


trait SinglePricePointAuction[T <: Tradable, +A <: SinglePricePointAuction[T, A]]
  extends Auction[T, SinglePricePointOrder[T], A] {
  this: A =>
}
