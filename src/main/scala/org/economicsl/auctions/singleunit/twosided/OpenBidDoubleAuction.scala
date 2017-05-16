package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{DiscriminatoryPricing, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.PriceQuotingPolicy


trait OpenBidDoubleAuction[T <: Tradable] extends SealedBidDoubleAuction[T] {

  def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
    quotingPolicy(orderBook, request)
  }

  private[this] val quotingPolicy: PriceQuotingPolicy[T] = new PriceQuotingPolicy[T]

}


object OpenBidDoubleAuction {

  def withDiscriminatoryPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): OpenBidDoubleAuction[T] = {
    new DiscriminatoryPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }

  def withUniformPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): OpenBidDoubleAuction[T] = {
    new UniformPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }

  private case class DiscriminatoryPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends OpenBidDoubleAuction[T]


  private object DiscriminatoryPricingImpl {

    implicit def auction[T <: Tradable]: DoubleAuctionLike[T, OpenBidDoubleAuction[T]] with DiscriminatoryPricing[T, OpenBidDoubleAuction[T]] = {

      new DoubleAuctionLike[T, OpenBidDoubleAuction[T]] with DiscriminatoryPricing[T, OpenBidDoubleAuction[T]] {

        def insert(a: OpenBidDoubleAuction[T], order: AskOrder[T]): OpenBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: OpenBidDoubleAuction[T], order: BidOrder[T]): OpenBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: OpenBidDoubleAuction[T], order: AskOrder[T]): OpenBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: OpenBidDoubleAuction[T], order: BidOrder[T]): OpenBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: OpenBidDoubleAuction[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: OpenBidDoubleAuction[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: OpenBidDoubleAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }


  private case class UniformPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends OpenBidDoubleAuction[T]


  private object UniformPricingImpl {

    implicit def auction[T <: Tradable]: DoubleAuctionLike[T, OpenBidDoubleAuction[T]] with UniformPricing[T, OpenBidDoubleAuction[T]] = {

      new DoubleAuctionLike[T, OpenBidDoubleAuction[T]] with UniformPricing[T, OpenBidDoubleAuction[T]] {

        def insert(a: OpenBidDoubleAuction[T], order: AskOrder[T]): OpenBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: OpenBidDoubleAuction[T], order: BidOrder[T]): OpenBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: OpenBidDoubleAuction[T], order: AskOrder[T]): OpenBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: OpenBidDoubleAuction[T], order: BidOrder[T]): OpenBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: OpenBidDoubleAuction[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: OpenBidDoubleAuction[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: OpenBidDoubleAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidDoubleAuction[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }

}