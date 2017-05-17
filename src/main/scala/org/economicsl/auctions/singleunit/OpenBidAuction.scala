package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}


class OpenBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])


object OpenBidAuction {

  implicit def openAuctionLikeOps[T <: Tradable](a: OpenBidAuction[T]): OpenAuctionLikeOps[T, OpenBidAuction[T]] = {
    new OpenAuctionLikeOps[T, OpenBidAuction[T]](a)
  }

  implicit def openAuctionLike[T <: Tradable]: OpenAuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] = {

    new OpenAuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] {

      def insert(a: OpenBidAuction[T], order: BidOrder[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def receive(a: OpenBidAuction[T], request: BidPriceQuoteRequest): Option[BidPriceQuote] = {
        bidPriceQuotingPolicy(a.orderBook, request)
      }

      def remove(a: OpenBidAuction[T], order: BidOrder[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: OpenBidAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: OpenBidAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: OpenBidAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

  def apply[T <: Tradable](reservation: AskOrder[T], pricingPolicy: PricingPolicy[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

  def withHighestPricingPolicy[T <: Tradable](reservation: AskOrder[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy[T])
  }

  def withSecondHighestPricingPolicy[T <: Tradable](reservation: AskOrder[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy[T])
  }

}
