package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.UniformClearingPolicy2
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy
import org.economicsl.core.Tradable


final class FirstPriceSealedBidAuction[T <: Tradable] private(
  protected val orderBook: FourHeapOrderBook[T],
  tickSize: Long)
    extends SealedBidAuction2[T, FirstPriceSealedBidAuction[T]]
    with UniformClearingPolicy2[T, FirstPriceSealedBidAuction[T]] {

  protected def withOrderBook(orderBook: FourHeapOrderBook[T]): FirstPriceSealedBidAuction[T] = {
    new FirstPriceSealedBidAuction(orderBook, tickSize)
  }

  protected val pricingPolicy: AskQuotePricingPolicy[T] = new AskQuotePricingPolicy[T]

}


object FirstPriceSealedBidAuction {


}
