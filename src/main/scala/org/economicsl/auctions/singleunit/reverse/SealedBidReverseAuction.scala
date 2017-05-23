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
package org.economicsl.auctions.singleunit.reverse

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidReverseAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidReverseAuction {

  implicit def reverseAuctionLikeOps[T <: Tradable](a: SealedBidReverseAuction[T]): ReverseAuctionLike.Ops[T, SealedBidReverseAuction[T]] = {
    new ReverseAuctionLike.Ops[T, SealedBidReverseAuction[T]](a)
  }

  implicit def reverseAuctionLike[T <: Tradable]: ReverseAuctionLike[T, SealedBidReverseAuction[T]] with UniformPricing[T, SealedBidReverseAuction[T]] = {

    new ReverseAuctionLike[T, SealedBidReverseAuction[T]] with UniformPricing[T, SealedBidReverseAuction[T]] {

      def insert(a: SealedBidReverseAuction[T], order: AskOrder[T]): SealedBidReverseAuction[T] = {
        new SealedBidReverseAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def remove(a: SealedBidReverseAuction[T], order: AskOrder[T]): SealedBidReverseAuction[T] = {
        new SealedBidReverseAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: SealedBidReverseAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: SealedBidReverseAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: SealedBidReverseAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidReverseAuction[T] = {
        new SealedBidReverseAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }
  
  
  def apply[T <: Tradable](reservation: BidOrder[T], pricingPolicy: PricingPolicy[T]): SealedBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidReverseAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

  /** Create a first-price, sealed-bid reverse auction (FPSBRA).
    *
    * @param reservation
    * @tparam T
    * @return
    * @note The winner of a FPSBRA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to its own ask price.
    */
  def withBidQuotePricingPolicy[T <: Tradable](reservation: BidOrder[T]): SealedBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidReverseAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy)
  }

  /** Create a second-price, sealed-bid reverse auction (SPSBRA).
    *
    * @param reservation
    * @tparam T
    * @return
    * @note The winner of a SPSBRA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to the minimum of the second lowest submitted ask price and the buyer's `reservation` price.
    */
  def withSecondLowestPricingPolicy[T <: Tradable](reservation: BidOrder[T]): SealedBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidReverseAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy)
  }
  
}
