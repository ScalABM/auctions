package org.economicsl.auctions.singleunit.pricing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.{Price, Tradable}


class WeightedAveragePricingRule[T <: Tradable](weight: Double) extends PricingRule[T, Price] {
  require(0.0 <= weight && weight <= 1.0)  // individual rationality requirement!

  def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
    orderBook.askPriceQuote.flatMap(p1 => orderBook.bidPriceQuote.map(p2 => Price(p2.value * weight + p1.value * (1 - weight))))  // todo fix this once there is support for numeric ops between prices!
  }

}
