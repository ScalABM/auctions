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
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.{PricingPolicy, UniformPricing}


class OpenBidReverseAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T], val pricingPolicy: PricingPolicy[T])


object OpenBidReverseAuction {

  implicit def openReverseAuctionLikeOps[T <: Tradable](a: OpenBidReverseAuction[T]): OpenReverseAuctionLike.Ops[T, OpenBidReverseAuction[T]] = {
    new OpenReverseAuctionLike.Ops[T, OpenBidReverseAuction[T]](a)
  }

  implicit def openReverseAuctionLike[T <: Tradable]: OpenReverseAuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]] = {

    new OpenReverseAuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]] {

      def insert(a: OpenBidReverseAuction[T], order: AskOrder[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](a.orderBook.insert(order), a.pricingPolicy)
      }

      def receive(a: OpenBidReverseAuction[T], request: BidPriceQuoteRequest): Option[BidPriceQuote] = {
        bidPriceQuotingPolicy(a.orderBook, request)
      }
      
      def remove(a: OpenBidReverseAuction[T], order: AskOrder[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](a.orderBook.remove(order), a.pricingPolicy)
      }

      def orderBook(a: OpenBidReverseAuction[T]): FourHeapOrderBook[T] = a.orderBook

      def pricingPolicy(a: OpenBidReverseAuction[T]): PricingPolicy[T] = a.pricingPolicy

      protected def withOrderBook(a: OpenBidReverseAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](orderBook, a.pricingPolicy)
      }

    }

  }

  def apply[T <: Tradable](reservation: BidOrder[T], pricingPolicy: PricingPolicy[T]): OpenBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidReverseAuction[T](orderBook.insert(reservation), pricingPolicy)
  }

}