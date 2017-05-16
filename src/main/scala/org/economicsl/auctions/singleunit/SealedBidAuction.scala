package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}


class SealedBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])


object SealedBidAuction {

  implicit def auction[T <: Tradable]: AuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] = {

    new AuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] {

      def insert(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def remove(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: SealedBidAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: SealedBidAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: SealedBidAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

  def apply[T <: Tradable](reservation: AskOrder[T], pricingPolicy: PricingPolicy[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

  def withHighestPricingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy[T])
  }

  def withSecondHighestPricingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy[T])
  }

}
