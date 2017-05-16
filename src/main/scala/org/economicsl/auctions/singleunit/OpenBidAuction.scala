package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}


class OpenBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])
  extends BidPriceQuoting {

  def receive(request: BidPriceQuoteRequest): Option[BidPriceQuote] = {
    quotingPolicy(orderBook, request)
  }

  private[this] val quotingPolicy = new BidPriceQuotingPolicy[T]

}


object OpenBidAuction {

  implicit def auction[T <: Tradable]: AuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] = {

    new AuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] {

      def insert(a: OpenBidAuction[T], order: BidOrder[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](a.orderBook.insert(order), a.pricingPolicy)
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
