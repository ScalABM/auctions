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

import org.economicsl.auctions.singleunit.clearing.{WithDiscriminatoryClearingPolicy, WithUniformClearingPolicy}
import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


/** Base trait for representing a "sealed-bid" double auction mechanism.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `SealedBidDoubleAuction` must be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
trait SealedBidDoubleAuction[T <: Tradable]
    extends Auction[T]


/** Companion object for the `SealedBidDoubleAuction` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidDoubleAuction {

  /** Create a "sealed-bid" double auction mechanism with discriminatory pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `SealedBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @return a `SealedBidDoubleAuction.DiscriminatoryPricingImpl` instance.
    */
  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (pricingPolicy: PricingPolicy[T], tickSize: Currency)
                                      : SealedBidDoubleAuction[T] = {
    DiscriminatoryPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Create a "sealed-bid" double auction mechanism with uniform pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `SealedBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @return a `SealedBidDoubleAuction.UniformPricingImpl` instance.
    */
  def withUniformPricing[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): SealedBidDoubleAuction[T] = {
    new UniformPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Type class representing an "sealed-bid" double auction mechanism with discriminatory pricing.
    *
    * @param orderBook a `FourHeapOrderBook` instance containing any previously submitted `AskOrder` and `BidOrder`
    *                  instances.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `SealedBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  private case class DiscriminatoryPricingImpl[T <: Tradable](
      orderBook: FourHeapOrderBook[T],
      pricingPolicy: PricingPolicy[T],
      tickSize: Currency)
    extends SealedBidDoubleAuction[T]


  /** Companion object for the `DiscriminatoryPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  private object DiscriminatoryPricingImpl {

    implicit def auctionLikeOps[T <: Tradable]
                               (a: DiscriminatoryPricingImpl[T])
                               : AuctionLike.Ops[T, DiscriminatoryPricingImpl[T]] = {
      new AuctionLike.Ops[T, DiscriminatoryPricingImpl[T]](a)
    }

    implicit def auctionLike[T <: Tradable]: WithDiscriminatoryClearingPolicy[T, SealedBidDoubleAuction[T]] = {
      new WithDiscriminatoryClearingPolicy[T, SealedBidDoubleAuction[T]] {
        protected def withOrderBook(a: SealedBidDoubleAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidDoubleAuction[T] = {
          new DiscriminatoryPricingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }
      }
    }

  }


  /** Type class representing a "sealed-bid" double auction mechanism with uniform pricing.
    *
    * @param orderBook a `FourHeapOrderBook` instance containing any previously submitted `AskOrder` and `BidOrder`
    *                  instances.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `SealedBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  private case class UniformPricingImpl[T <: Tradable](
      orderBook: FourHeapOrderBook[T],
      pricingPolicy: PricingPolicy[T],
      tickSize: Currency)
    extends SealedBidDoubleAuction[T]


  /** Companion object for the `UniformPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  private object UniformPricingImpl {

    implicit def auctionLikeOps[T <: Tradable](a: UniformPricingImpl[T]): AuctionLike.Ops[T, UniformPricingImpl[T]] = {
      new AuctionLike.Ops[T, UniformPricingImpl[T]](a)
    }

    implicit def auctionLike[T <: Tradable]: WithUniformClearingPolicy[T, UniformPricingImpl[T]] = {
      new WithUniformClearingPolicy[T, UniformPricingImpl[T]] {
        protected def withOrderBook(a: UniformPricingImpl[T], orderBook: FourHeapOrderBook[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }
      }
    }

  }

}
