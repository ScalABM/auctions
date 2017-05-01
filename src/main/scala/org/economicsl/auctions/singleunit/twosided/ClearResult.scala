package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.{Fill, ClearResultLike, Order}


case class ClearResult[T <: Tradable, +A <: DoubleAuction[T]](fills: Option[Stream[Fill[T]]], residual: A)
  extends ClearResultLike[T, Order[T], A]

