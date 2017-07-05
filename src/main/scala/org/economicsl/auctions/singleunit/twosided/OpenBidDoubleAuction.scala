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

import org.economicsl.auctions.singleunit.AuctionLike
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{DiscriminatoryPricing, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, BidPriceQuoting, SpreadQuoting}
import org.economicsl.core.{Currency, Tradable}


/** Base trait for representing an "open-bid" double auction mechanism.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidDoubleAuction[T <: Tradable]
    extends SealedBidDoubleAuction[T]
    with AskPriceQuoting[T]
    with BidPriceQuoting[T]
    with SpreadQuoting[T]

/** Companion object for the `OpenBidDoubleAuction` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidDoubleAuction {

  /** Create an "open-bid" double auction mechanism with discriminatory pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @return an `OpenBidDoubleAuction.DiscriminatoryPricingImpl` instance.
    */
  def withDiscriminatoryPricing[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): DiscriminatoryPricingImpl[T] = {
    new DiscriminatoryPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Create an "open-bid" double auction mechanism with uniform pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @return an `OpenBidDoubleAuction.UniformPricingImpl` instance.
    */
  def withUniformPricing[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): UniformPricingImpl[T] = {
    new UniformPricingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Type class representing an "open-bid" double auction mechanism with discriminatory pricing.
    *
    * @param orderBook a `FourHeapOrderBook` instance containing any previously submitted `AskOrder` and `BidOrder`
    *                  instances.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  case class DiscriminatoryPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T], tickSize: Currency)
    extends OpenBidDoubleAuction[T]


  /** Companion object for the `DiscriminatoryPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  object DiscriminatoryPricingImpl {

    implicit def doubleAuctionLikeOps[T <: Tradable](a: DiscriminatoryPricingImpl[T]): AuctionLike.Ops[T, DiscriminatoryPricingImpl[T]] = {
      new AuctionLike.Ops[T, DiscriminatoryPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]: AuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] = {

      new AuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] {

        protected def withOrderBook(a: DiscriminatoryPricingImpl[T], orderBook: FourHeapOrderBook[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }

      }

    }

  }


  /** Type class representing an "open-bid" double auction mechanism with uniform pricing.
    *
    * @param orderBook a `FourHeapOrderBook` instance containing any previously submitted `AskOrder` and `BidOrder`
    *                  instances.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  case class UniformPricingImpl[T <: Tradable](orderBook: FourHeapOrderBook[T], pricingPolicy: PricingPolicy[T], tickSize: Currency)
    extends OpenBidDoubleAuction[T]


  /** Companion object for the `UniformPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  object UniformPricingImpl {

    implicit def doubleAuctionLikeOps[T <: Tradable](a: UniformPricingImpl[T]): AuctionLike.Ops[T, UniformPricingImpl[T]] = {
      new AuctionLike.Ops[T, UniformPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]
                                  : AuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] = {

      new AuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] {

        protected def withOrderBook(a: UniformPricingImpl[T], orderBook: FourHeapOrderBook[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }

      }

    }

  }

}