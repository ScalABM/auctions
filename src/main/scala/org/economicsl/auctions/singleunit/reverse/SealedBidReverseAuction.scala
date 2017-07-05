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

import org.economicsl.auctions.Token
import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}
import org.economicsl.core.{Currency, Tradable}


/** Type class representing a "sealed-bid" reverse auction mechanism.
  *
  * @param orderBook a `FourHeapOrderBook` instance containing the reservation `BidOrder` and any previously submitted
  *                  `AskOrder` instances.
  * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
  * @param tickSize the minimum price movement of a tradable.
  * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `SealedBidReverseAuction`
  *           must be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
class SealedBidReverseAuction[T <: Tradable] private(
    protected[reverse] val orderBook: FourHeapOrderBook[T],
    val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency)
  extends Auction[T]


/** Companion object for the `SealedBidReverseAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidReverseAuction {

  import org.economicsl.auctions.singleunit.AuctionParticipant._

  implicit def reverseAuctionLikeOps[T <: Tradable]
                                    (a: SealedBidReverseAuction[T])
                                    : AuctionLike.Ops[T, SealedBidReverseAuction[T]] = {
    new AuctionLike.Ops[T, SealedBidReverseAuction[T]](a)
  }

  implicit def reverseAuctionLike[T <: Tradable]
                                 : AuctionLike[T, SealedBidReverseAuction[T]] with UniformPricing[T, SealedBidReverseAuction[T]] = {
    new AuctionLike[T, SealedBidReverseAuction[T]] with UniformPricing[T, SealedBidReverseAuction[T]] {
      protected def withOrderBook(a: SealedBidReverseAuction[T], orderBook: FourHeapOrderBook[T]): SealedBidReverseAuction[T] = {
        new SealedBidReverseAuction[T](orderBook, a.pricingPolicy, a.tickSize)
      }
    }
  }

  /** Create an instance of a "sealed-bid" reverse auction mechanism.
    *
    * @param reservation a `BidOrder` instance representing the reservation price for the reverse auction.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `SealedBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return a `SealedBidReverseAuction` instance.
    */
  def withReservation[T <: Tradable]
                     (reservation: (Token, BidOrder[T]), pricingPolicy: PricingPolicy[T], tickSize: Currency)
                     : (SealedBidReverseAuction[T], Either[Rejected, Accepted]) = {
    val orderBook = FourHeapOrderBook.empty[T]
    val auction = new SealedBidReverseAuction[T](orderBook, pricingPolicy, tickSize)
    auction.insert(reservation)
  }

  /** Create a second-price, sealed-bid reverse auction (SPSBRA).
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `SealedBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return a `SealedBidReverseAuction` instance.
    * @note The winner of a SPSBRA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to the minimum of the second lowest submitted ask price and the buyer's `reservation` price.
    */
  def withAskQuotePricingPolicy[T <: Tradable](tickSize: Currency): SealedBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidReverseAuction[T](orderBook, new AskQuotePricingPolicy[T], tickSize)
  }

  /** Create a first-price, sealed-bid reverse auction (FPSBRA).
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `SealedBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return a `SealedBidReverseAuction` instance.
    * @note The winner of a FPSBRA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to its own ask price.
    */
  def withBidQuotePricingPolicy[T <: Tradable](tickSize: Currency): SealedBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidReverseAuction[T](orderBook, new BidQuotePricingPolicy[T], tickSize)
  }
  
}
