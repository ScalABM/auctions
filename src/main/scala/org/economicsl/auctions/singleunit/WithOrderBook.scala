package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook


abstract class WithOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) {

  def insert(order: LimitBidOrder[T]): WithOrderBook[T]

  def remove(order: LimitBidOrder[T]): WithOrderBook[T]

}

