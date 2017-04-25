package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{Order, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


trait AuctionLike[T <: Tradable, O <: Order[T] with SingleUnit[T], A <: AuctionLike[T, O, A]] {

  def insert(order: O): A

  def remove(order: O): A

  def clear: (Option[Stream[Fill[T]]], A)

  protected def orderBook: FourHeapOrderBook[T]

  protected def pricing: PricingPolicy[T]

}