/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{DiscriminatoryPricing, PricingPolicy, UniformPricing}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SealedBidDoubleAuction[T <: Tradable] {

  def orderBook: FourHeapOrderBook[T]

  def pricingPolicy: PricingPolicy[T]

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidDoubleAuction {

  def withDiscriminatoryPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): DiscriminatoryPricingImpl[T] = {
    new DiscriminatoryPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }

  def withUniformPricing[T <: Tradable](pricingPolicy: PricingPolicy[T]): UniformPricingImpl[T] = {
    new UniformPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy)
  }

  case class DiscriminatoryPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends SealedBidDoubleAuction[T]

  object DiscriminatoryPricingImpl {

    implicit def doubleAuctionLikeOps[T <: Tradable](a: DiscriminatoryPricingImpl[T]): DoubleAuctionLike.Ops[T, DiscriminatoryPricingImpl[T]] = {
      new DoubleAuctionLike.Ops[T, DiscriminatoryPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]: DoubleAuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] = {

      new DoubleAuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] {

        def insert(a: DiscriminatoryPricingImpl[T], order: AskOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: DiscriminatoryPricingImpl[T], order: BidOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: DiscriminatoryPricingImpl[T], order: AskOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: DiscriminatoryPricingImpl[T], order: BidOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: DiscriminatoryPricingImpl[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: DiscriminatoryPricingImpl[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: DiscriminatoryPricingImpl[T], orderBook: FourHeapOrderBook[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }


  case class UniformPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T])
    extends SealedBidDoubleAuction[T]


  object UniformPricingImpl {

    implicit def doubleAuctionLikeOps[T <: Tradable](a: UniformPricingImpl[T]): DoubleAuctionLike.Ops[T, UniformPricingImpl[T]] = {
      new DoubleAuctionLike.Ops[T, UniformPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]: DoubleAuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] = {

      new DoubleAuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] {

        def insert(a: UniformPricingImpl[T], order: AskOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def insert(a: UniformPricingImpl[T], order: BidOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy)
        }

        def remove(a: UniformPricingImpl[T], order: AskOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def remove(a: UniformPricingImpl[T], order: BidOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy)
        }

        def orderBook(a: UniformPricingImpl[T]): FourHeapOrderBook[T] = a.orderBook

        def pricingPolicy(a: UniformPricingImpl[T]): PricingPolicy[T] = a.pricingPolicy

        protected def withOrderBook(a: UniformPricingImpl[T], orderBook: FourHeapOrderBook[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy)
        }

      }

    }

  }

}
