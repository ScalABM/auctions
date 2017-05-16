package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{DiscriminatoryPricing, PricingPolicy, UniformPricing}


trait SealedBidDoubleAuction[T <: Tradable] {

  def orderBook: FourHeapOrderBook[T]

  def pricingPolicy: PricingPolicy[T]

}


object SealedBidDoubleAuction {

  def withDiscriminatoryPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): SealedBidDoubleAuction[T] = {
    new DiscriminatoryPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }

  def withUniformPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): SealedBidDoubleAuction[T] = {
    new UniformPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }



  private case class DiscriminatoryPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends SealedBidDoubleAuction[T]


  private object DiscriminatoryPricingImpl {

    implicit def auction[T <: Tradable]: DoubleAuctionLike[T, SealedBidDoubleAuction[T]] with DiscriminatoryPricing[T, SealedBidDoubleAuction[T]] = {

      new DoubleAuctionLike[T, SealedBidDoubleAuction[T]] with DiscriminatoryPricing[T, SealedBidDoubleAuction[T]] {

        def insert(a: SealedBidDoubleAuction[T], order: AskOrder[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: SealedBidDoubleAuction[T], order: BidOrder[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: SealedBidDoubleAuction[T], order: AskOrder[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: SealedBidDoubleAuction[T], order: BidOrder[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: SealedBidDoubleAuction[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: SealedBidDoubleAuction[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: SealedBidDoubleAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }


  private case class UniformPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends SealedBidDoubleAuction[T]


  private object UniformPricingImpl {

    implicit def auction[T <: Tradable]: DoubleAuctionLike[T, SealedBidDoubleAuction[T]] with UniformPricing[T, SealedBidDoubleAuction[T]] = {

      new DoubleAuctionLike[T, SealedBidDoubleAuction[T]] with UniformPricing[T, SealedBidDoubleAuction[T]] {

        def insert(a: SealedBidDoubleAuction[T], order: AskOrder[T]): SealedBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: SealedBidDoubleAuction[T], order: BidOrder[T]): SealedBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: SealedBidDoubleAuction[T], order: AskOrder[T]): SealedBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: SealedBidDoubleAuction[T], order: BidOrder[T]): SealedBidDoubleAuction[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: SealedBidDoubleAuction[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: SealedBidDoubleAuction[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: SealedBidDoubleAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidDoubleAuction[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }

}
