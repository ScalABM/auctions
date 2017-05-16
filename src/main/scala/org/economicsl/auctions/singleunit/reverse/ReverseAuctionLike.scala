package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.ClearResult
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


trait ReverseAuctionLike[T <: Tradable, A] {

  def insert(a: A, order: AskOrder[T]): A

  def remove(a: A, order: AskOrder[T]): A

  def clear(a: A): ClearResult[T, A]

  def orderBook(a: A): FourHeapOrderBook[T]

  def pricingPolicy(a: A): PricingPolicy[T]

}
