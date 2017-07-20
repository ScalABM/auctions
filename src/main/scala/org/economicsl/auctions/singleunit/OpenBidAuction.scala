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

import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.singleunit.clearing.{DiscriminatoryClearingPolicy, UniformClearingPolicy}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


/** Base trait for all "Open-bid" auction implementations.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
  *           type of `Tradable`.
  * @note `OpenBidAuction` is an abstract class rather than a trait in order to facilitate Java interop. Specifically
  *      abstract class implementation allows Java methods to access the methods defined on the `OpenBidAuction`
  *      companion object from a static context.
  * @author davidrpugh
  * @since 0.1.0
  */
abstract class OpenBidAuction[T <: Tradable]
    extends Auction[T, OpenBidAuction[T]] {
  this: OpenBidAuction[T] =>

  def receive(request: PriceQuoteRequest[T]): PriceQuote = {
    request.query(orderBook)
  }

}


object OpenBidAuction {

  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (pricingPolicy: PricingPolicy[T], tickSize: Currency, tradable: T)
                                      : OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithDiscriminatoryClearingPolicy[T](orderBook, pricingPolicy, tickSize, tradable)
  }

  def withDiscriminatoryClearingPolicy[T <: Tradable]
                                      (pricingPolicy: PricingPolicy[T], tradable: T)
                                      : OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithDiscriminatoryClearingPolicy[T](orderBook, pricingPolicy, 1L, tradable)
  }

  def withUniformClearingPolicy[T <: Tradable]
                               (pricingPolicy: PricingPolicy[T], tickSize: Currency, tradable: T)
                               : OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithUniformClearingPolicy[T](orderBook, pricingPolicy, tickSize, tradable)
  }

  def withUniformClearingPolicy[T <: Tradable](pricingPolicy: PricingPolicy[T], tradable: T): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithUniformClearingPolicy[T](orderBook, pricingPolicy, 1L, tradable)
  }


  private class WithDiscriminatoryClearingPolicy[T <: Tradable](
    protected val orderBook: FourHeapOrderBook[T],
    protected val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency,
    val tradable: T)
      extends OpenBidAuction[T]
      with DiscriminatoryClearingPolicy[T, OpenBidAuction[T]] {

    /** Returns an auction of type `A` with a particular pricing policy. */
    def withPricingPolicy(updated: PricingPolicy[T]): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](orderBook, updated, tickSize, tradable)
    }

    /** Returns an auction of type `A` with a particular tick size. */
    def withTickSize(updated: Currency): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](orderBook, pricingPolicy, updated, tradable)
    }

    /** Factory method used by sub-classes to create an `Auction` of type `A`. */
    protected def withOrderBook(updated: FourHeapOrderBook[T]): OpenBidAuction[T] = {
      new WithDiscriminatoryClearingPolicy[T](updated, pricingPolicy, tickSize, tradable)
    }

  }


  private class WithUniformClearingPolicy[T <: Tradable](
    protected val orderBook: FourHeapOrderBook[T],
    protected val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency,
    val tradable: T)
      extends OpenBidAuction[T]
      with UniformClearingPolicy[T, OpenBidAuction[T]] {

    /** Returns an auction of type `A` with a particular pricing policy. */
    def withPricingPolicy(updated: PricingPolicy[T]): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](orderBook, updated, tickSize, tradable)
    }

    /** Returns an auction of type `A` with a particular tick size. */
    def withTickSize(updated: Currency): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](orderBook, pricingPolicy, updated, tradable)
    }

    /** Factory method used by sub-classes to create an `Auction` of type `A`. */
    protected def withOrderBook(updated: FourHeapOrderBook[T]): OpenBidAuction[T] = {
      new WithUniformClearingPolicy[T](updated, pricingPolicy, tickSize, tradable)
    }

  }

}