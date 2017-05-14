package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


/** Mixin trait providing behaviors relevant for auctions. */
trait AuctionLike[T <: Tradable, O <: Order[T], +A <: AuctionLike[T, O, A]] {

  def insert(order: O): A

  def remove(order: O): A

  protected def orderBook: FourHeapOrderBook[T]

  protected def pricingPolicy: PricingPolicy[T]

}
