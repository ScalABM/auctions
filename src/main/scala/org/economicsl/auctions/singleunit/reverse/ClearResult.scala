package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.{AskOrder, Fill, ClearResultLike}


case class ClearResult[T <: Tradable, +A <: ReverseAuction[T]](fills: Option[Stream[Fill[T]]], residual: A)
  extends ClearResultLike[T, AskOrder[T] ,A]

