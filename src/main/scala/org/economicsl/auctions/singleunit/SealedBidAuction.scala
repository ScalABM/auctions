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


/** Type class for implementing sealed-bid auctions.
  *
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

  /** Defines `AuctionLike` methods for the `SealedBidAuction` type class.
    *
    * @param a an instance of the `SealedBidAuction` type class.
    * @tparam T a type of `Tradable`.
    * @return an instance of `AuctionLike.Ops` that is used by the compiler to generate `AuctionLike` methods for
    *         instances of the `SealedBudAuction` class.
    */
  implicit def auctionLikeOps[T <: Tradable](a: SealedBidAuction[T]): AuctionLike.Ops[T, SealedBidAuction[T]] = {
    new AuctionLike.Ops[T, SealedBidAuction[T]](a)
  }

  implicit def auctionLike[T <: Tradable]: AuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] = {

    new AuctionLike[T, SealedBidAuction[T]] with UniformPricing[T, SealedBidAuction[T]] {

      def insert(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def remove(a: SealedBidAuction[T], order: BidOrder[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: SealedBidAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: SealedBidAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: SealedBidAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidAuction[T] = {
        new SealedBidAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

  def apply[T <: Tradable](reservation: AskOrder[T], pricingPolicy: PricingPolicy[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

  def withAskPriceQuotingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy[T])
  }

  def withBidPriceQuotingPolicy[T <: Tradable](reservation: AskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy[T])
  }

}
