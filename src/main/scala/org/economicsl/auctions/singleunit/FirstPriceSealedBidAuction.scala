package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.UniformClearingPolicy
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy
import org.economicsl.core.{Currency, Tradable}


final class FirstPriceSealedBidAuction[T <: Tradable] private(
  orderBook: FourHeapOrderBook[T],
  tickSize: Long)
    extends SealedBidAuction[T, FirstPriceSealedBidAuction[T]](orderBook, new AskQuotePricingPolicy[T], tickSize)
    with UniformClearingPolicy[T, FirstPriceSealedBidAuction[T]] {

  protected def withOrderBook(orderBook: FourHeapOrderBook[T]): FirstPriceSealedBidAuction[T] = {
    new FirstPriceSealedBidAuction(orderBook, tickSize)
  }

}


object FirstPriceSealedBidAuction {

  def withTickSize[T <: Tradable](tickSize: Currency): FirstPriceSealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new FirstPriceSealedBidAuction(orderBook, tickSize)
  }

}
