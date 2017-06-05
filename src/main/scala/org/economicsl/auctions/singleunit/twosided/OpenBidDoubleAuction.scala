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

import org.economicsl.auctions.{Currency, Tradable}
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{DiscriminatoryPricing, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.twosided.OpenBidDoubleAuctionLike.Ops

import scala.util.Try


/** Base trait for representing an "open-bid" double auction mechanism.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidDoubleAuction` must be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
trait OpenBidDoubleAuction[T <: Tradable] extends SealedBidDoubleAuction[T]


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

    implicit def doubleAuctionLikeOps[T <: Tradable](a: DiscriminatoryPricingImpl[T]): Ops[T, DiscriminatoryPricingImpl[T]] = {
      new Ops[T, DiscriminatoryPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]: OpenBidDoubleAuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] = {

      new OpenBidDoubleAuctionLike[T, DiscriminatoryPricingImpl[T]] with DiscriminatoryPricing[T, DiscriminatoryPricingImpl[T]] {

        def insert(a: DiscriminatoryPricingImpl[T], order: AskOrder[T]): Try[DiscriminatoryPricingImpl[T]] = Try {
          require(order.limit.value % a.tickSize == 0)
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy, a.tickSize)
        }

        def insert(a: DiscriminatoryPricingImpl[T], order: BidOrder[T]): Try[DiscriminatoryPricingImpl[T]] = Try {
          require(order.limit.value % a.tickSize == 0)
          new DiscriminatoryPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy, a.tickSize)
        }

        def receive(a: DiscriminatoryPricingImpl[T], request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = {
          askPriceQuotingPolicy(a.orderBook, request)
        }

        def receive(a: DiscriminatoryPricingImpl[T], request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = {
          bidPriceQuotingPolicy(a.orderBook, request)
        }

        def receive(a: DiscriminatoryPricingImpl[T], request: SpreadQuoteRequest[T]): Option[SpreadQuote] = {
          spreadQuotingPolicy(a.orderBook, request)
        }

        def remove(a: DiscriminatoryPricingImpl[T], order: AskOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy, a.tickSize)
        }

        def remove(a: DiscriminatoryPricingImpl[T], order: BidOrder[T]): DiscriminatoryPricingImpl[T] = {
          new DiscriminatoryPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy, a.tickSize)
        }

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

    implicit def doubleAuctionLikeOps[T <: Tradable](a: UniformPricingImpl[T]): Ops[T, UniformPricingImpl[T]] = {
      new Ops[T, UniformPricingImpl[T]](a)
    }

    implicit def doubleAuctionLike[T <: Tradable]: OpenBidDoubleAuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] = {

      new OpenBidDoubleAuctionLike[T, UniformPricingImpl[T]] with UniformPricing[T, UniformPricingImpl[T]] {

        def insert(a: UniformPricingImpl[T], order: AskOrder[T]): Try[UniformPricingImpl[T]] = Try {
          require(order.limit.value % a.tickSize == 0)
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy, a.tickSize)
        }

        def insert(a: UniformPricingImpl[T], order: BidOrder[T]): Try[UniformPricingImpl[T]] = Try {
          require(order.limit.value % a.tickSize == 0)
          new UniformPricingImpl[T](a.orderBook.insert(order), a.pricingPolicy, a.tickSize)
        }

        def receive(a: UniformPricingImpl[T], request: AskPriceQuoteRequest[T]): Option[AskPriceQuote] = {
          askPriceQuotingPolicy(a.orderBook, request)
        }

        def receive(a: UniformPricingImpl[T], request: BidPriceQuoteRequest[T]): Option[BidPriceQuote] = {
          bidPriceQuotingPolicy(a.orderBook, request)
        }

        def receive(a: UniformPricingImpl[T], request: SpreadQuoteRequest[T]): Option[SpreadQuote] = {
          spreadQuotingPolicy(a.orderBook, request)
        }

        def remove(a: UniformPricingImpl[T], order: AskOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy, a.tickSize)
        }

        def remove(a: UniformPricingImpl[T], order: BidOrder[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](a.orderBook.remove(order), a.pricingPolicy, a.tickSize)
        }

        protected def withOrderBook(a: UniformPricingImpl[T], orderBook: FourHeapOrderBook[T]): UniformPricingImpl[T] = {
          new UniformPricingImpl[T](orderBook, a.pricingPolicy, a.tickSize)
        }

      }

    }

  }

}