package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


trait Auction[T <: Tradable] {

  def orderBook: FourHeapOrderBook[T]

  def pricingPolicy: PricingPolicy[T]

  def tickSize: Currency

}
