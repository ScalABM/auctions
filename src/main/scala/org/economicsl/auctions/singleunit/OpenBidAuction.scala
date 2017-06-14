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

import org.economicsl.auctions.{Currency, Tradable}
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}

import scala.util.Try


/** Type class representing an "open-bid" auction mechanism.
  *
  * @param orderBook a `FourHeapOrderBook` instance containing the reservation `AskOrder` and any previously submitted
  *                  `BidOrder` instances.
  * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
  * @param tickSize the minimum price movement of a tradable.
  * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
  *           be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
class OpenBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T], val tickSize: Currency)


/** Companion object for the `OpenBidAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidAuction {

  implicit def mkAuctionOps[T <: Tradable](a: OpenBidAuction[T]): OpenBidAuctionLike.Ops[T, OpenBidAuction[T]] = {
    new OpenBidAuctionLike.Ops[T, OpenBidAuction[T]](a)
  }

  implicit def mkAuctionLike[T <: Tradable]: OpenBidAuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] = {

    new OpenBidAuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] {

      def insert(a: OpenBidAuction[T], order: BidOrder[T]): Try[OpenBidAuction[T]] = Try {
        require(order.limit.value % a.tickSize == 0)
        new OpenBidAuction[T](a.orderBook.insert(order), a.pricingPolicy, a.tickSize)
      }

      def receive(a: OpenBidAuction[T], request: AskPriceQuoteRequest[T]): AskPriceQuote = {
        askPriceQuotingPolicy(a.orderBook, request)
      }

      def remove(a: OpenBidAuction[T], order: BidOrder[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](a.orderBook.remove(order), a.pricingPolicy, a.tickSize)
      }

      protected def withOrderBook(a: OpenBidAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidAuction[T] = {
        new OpenBidAuction[T](orderBook, a.pricingPolicy, a.tickSize)
      }

    }

  }

  /** Create an instance of an "open-bid" auction mechanism.
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return an `OpenBidAuction` instance.
    */
  def apply[T <: Tradable](reservation: AskOrder[T], pricingPolicy: PricingPolicy[T], tickSize: Currency): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), pricingPolicy, tickSize)
  }

  /** Create an instance of a first-price, open-bid auction (FPOBA) mechanism.
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return an `OpenBidAuction` instance.
    */
  def withAskQuotePricingPolicy[T <: Tradable](reservation: AskOrder[T], tickSize: Currency): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy[T], tickSize)
  }

  /** Create an instance of a second-price, open-bid auction (SPOBA) mechanism.
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return an `OpenBidAuction` instance.
    */
  def withBidQuotePricingPolicy[T <: Tradable](reservation: AskOrder[T], tickSize: Currency): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy[T], tickSize)
  }

}
