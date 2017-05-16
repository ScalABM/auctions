package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.pricing.{PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.AskPriceQuotingPolicy


class OpenBidReverseAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T]) {
  
  def receive(request: AskPriceQuoteRequest): Option[AskPriceQuote] = {
    quotingPolicy(orderBook, request)
  }

  private[this] val quotingPolicy = new AskPriceQuotingPolicy[T]

}


object OpenBidReverseAuction {

  implicit def auction[T <: Tradable]: ReverseAuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]] = {

    new ReverseAuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]]{

      def insert(a: OpenBidReverseAuction[T], order: AskOrder[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }
      
      def remove(a: OpenBidReverseAuction[T], order: AskOrder[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: OpenBidReverseAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: OpenBidReverseAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: OpenBidReverseAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

}