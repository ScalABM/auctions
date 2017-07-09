package org.economicsl.auctions.singleunit

import org.economicsl.auctions.quotes.PriceQuoteRequest
import org.economicsl.auctions.singleunit.clearing.UniformClearingPolicy
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy
import org.economicsl.auctions.singleunit.quoting.PriceQuotingPolicy
import org.economicsl.core.{Currency, Tradable}


class FirstPriceOpenBidAuction[T <: Tradable] private(
  orderBook: FourHeapOrderBook[T],
  tickSize: Long)
    extends OpenBidAuction[T, FirstPriceOpenBidAuction[T]](orderBook, new AskQuotePricingPolicy[T], tickSize)
    with UniformClearingPolicy[T, FirstPriceOpenBidAuction[T]]
    with PriceQuotingPolicy[T, FirstPriceOpenBidAuction[T]] {

  protected def withOrderBook(updated: FourHeapOrderBook[T]): FirstPriceOpenBidAuction[T] = {
    new FirstPriceOpenBidAuction(updated, tickSize)
  }

}


object FirstPriceOpenBidAuction {

  def withTickSize[T <: Tradable](tickSize: Currency): FirstPriceOpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new FirstPriceOpenBidAuction[T](orderBook, tickSize)
  }

}
