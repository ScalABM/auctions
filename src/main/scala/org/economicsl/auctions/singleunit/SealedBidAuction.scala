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

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}


/** Type class representing a "sealed-bid" auction mechanism.
  *
  * @param orderBook a `FourHeapOrderBook` instance containing the reservation `AskOrder` and any previously submitted
  *                  `BidOrder` instances.
  * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
  * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `SealedBidAuction` must
  *           be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])


/** Companion object for the `SealedBidAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidAuction {

  /** Create an instance of `SealedBidAuctionLike.Ops`.
    *
    * @param a an instance of the `SealedBidAuction` type class.
    * @tparam T all `BidOrder` instances processed by a `SealedBidAuction` must be for the same type of `Tradable`.
    * @return an instance of `SealedBidAuctionLike.Ops` that will be used by the compiler to generate the
    *         `SealedBidAuctionLike` methods for the `SealedBidAuction` type class.
    */
  implicit def mkAuctionOps[T <: Tradable](a: SealedBidAuction[T]): SealedBidAuctionLike.Ops[T, SealedBidAuction[T]] = {
    new SealedBidAuctionLike.Ops[T, SealedBidAuction[T]](a)
  }

  /** Create an instance of `SealedBidAuctionLike` trait.
    *
    * @tparam T all `BidOrder` instances processed by a `SealedBidAuction` must be for the same type of `Tradable`.
    * @return an instance of the `SealedBidAuctionLike` trait that will be used by the compiler to generate the
    *         `SealedBidAuctionLike` methods for the `SealedBidAuction` type class.
    */
  implicit def mkAuctionLike[T <: Tradable]: SealedBidAuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] = {

    new SealedBidAuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] {

      def insert(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def remove(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      protected def withOrderBook(a: SealedBidAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

  /** Create a "Sealed-bid" auction mechanism.
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    */
  def apply[T <: Tradable](reservation: AskOrder[T], pricingPolicy: PricingPolicy[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

  /** Create a "First-Price, Sealed-Bid Auction."
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    */
  def withAskPriceQuotingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy[T])
  }

  /** Create a "Second-Price, Sealed-Bid Auction."
    *
    * @param reservation an `AskOrder` instance representing the reservation price for the auction.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return a `SealedBidAuction` instance.
    * @note Second-Price, Sealed-Bid Auctions are also known as "Vickery Auctions."
    */
  def withBidPriceQuotingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy[T])
  }

}
