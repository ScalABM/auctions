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
package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.{WithDiscriminatoryClearingPolicy, WithUniformClearingPolicy}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, BidPriceQuoting, SpreadQuoting}
import org.economicsl.core.{Currency, Tradable}


/** Base trait for representing an "open-bid" double auction mechanism.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidAuction[T <: Tradable]
    extends SealedBidAuction[T]
    with AskPriceQuoting[T]
    with BidPriceQuoting[T]
    with SpreadQuoting[T]


/** Companion object for the `OpenBidAuction` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidAuction {

  /** Create an "open-bid" double auction mechanism with discriminatory pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
    *           type of `Tradable`.
    * @return an `OpenBidDoubleAuction.DiscriminatoryPricingImpl` instance.
    */
  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (pricingPolicy: PricingPolicy[T], tickSize: Currency)
                                      : DiscriminatoryClearingImpl[T] = {
    new DiscriminatoryClearingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Create an "open-bid" double auction mechanism with uniform pricing.
    *
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
    *           type of `Tradable`.
    * @return an `OpenBidDoubleAuction.UniformPricingImpl` instance.
    */
  def withUniformClearingPolicy[T <: Tradable]
                               (pricingPolicy: PricingPolicy[T], tickSize: Currency)
                               : UniformClearingImpl[T] = {
    new UniformClearingImpl[T](FourHeapOrderBook.empty, pricingPolicy, tickSize)
  }

  /** Type class representing an "open-bid" double auction mechanism with discriminatory pricing.
    *
    * @param orderBook a `FourHeapOrderBook` instance containing any previously submitted `AskOrder` and `BidOrder`
    *                  instances.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  case class DiscriminatoryClearingImpl[T <: Tradable](
      orderBook: FourHeapOrderBook[T],
      pricingPolicy: PricingPolicy[T],
      tickSize: Currency)
    extends OpenBidAuction[T]


  /** Companion object for the `DiscriminatoryPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  private object DiscriminatoryClearingImpl {

    implicit def auctionLikeOps[T <: Tradable](a: DiscriminatoryClearingImpl[T])
                                              : AuctionLike.Ops[T, DiscriminatoryClearingImpl[T]] = {
      new AuctionLike.Ops[T, DiscriminatoryClearingImpl[T]](a)
    }

    implicit def auctionLike[T <: Tradable]: WithDiscriminatoryClearingPolicy[T, DiscriminatoryClearingImpl[T]] = {
      new WithDiscriminatoryClearingPolicy[T, DiscriminatoryClearingImpl[T]] {
        protected def withOrderBook(a: DiscriminatoryClearingImpl[T], orderBook: FourHeapOrderBook[T]): DiscriminatoryClearingImpl[T] = {
          new DiscriminatoryClearingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
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
    * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
    *           type of `Tradable`.
    * @author davidrpugh
    * @since 0.1.0
    */
  case class UniformClearingImpl[T <: Tradable](
      orderBook: FourHeapOrderBook[T],
      pricingPolicy: PricingPolicy[T],
      tickSize: Currency)
    extends OpenBidAuction[T]


  /** Companion object for the `UniformPricingImpl` type class.
    *
    * @author davidrpugh
    * @since 0.1.0
    */
  private object UniformClearingImpl {

    implicit def auctionLikeOps[T <: Tradable](a: UniformClearingImpl[T]): AuctionLike.Ops[T, UniformClearingImpl[T]] = {
      new AuctionLike.Ops[T, UniformClearingImpl[T]](a)
    }

    implicit def auctionLike[T <: Tradable]: WithUniformClearingPolicy[T, UniformClearingImpl[T]] = {
      new WithUniformClearingPolicy[T, UniformClearingImpl[T]] {
        protected def withOrderBook(a: UniformClearingImpl[T], orderBook: FourHeapOrderBook[T]): UniformClearingImpl[T] = {
          new UniformClearingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }
      }
    }

  }

}