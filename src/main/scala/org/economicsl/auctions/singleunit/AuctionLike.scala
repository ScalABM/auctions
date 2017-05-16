package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


trait AuctionLike[T <: Tradable, A] {

  def insert(a: A, order: BidOrder[T]): A

  def remove(a: A, order: BidOrder[T]): A

  def clear(a: A): ClearResult[T, A]

  def orderBook(a: A): FourHeapOrderBook[T]

  def pricingPolicy(a: A): PricingPolicy[T]

}
