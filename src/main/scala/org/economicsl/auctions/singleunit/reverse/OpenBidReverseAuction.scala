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
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}
import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.{BidPriceQuoting, BidPriceQuotingPolicy}
import org.economicsl.core.{Currency, Tradable}


/** Type class representing an "open-bid" reverse auction mechanism.
  *
  * @param orderBook a `FourHeapOrderBook` instance containing the reservation `BidOrder` and any previously submitted
  *                  `AskOrder` instances.
  * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
  * @param tickSize the minimum price movement of a tradable.
  * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `OpenBidReverseAuction`
  *           must be for the same type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
class OpenBidReverseAuction[T <: Tradable] private(
    protected[singleunit] val orderBook: FourHeapOrderBook[T],
    val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency)
  extends Auction[T]
  with BidPriceQuoting[T]


/** Companion object fo the `OpenBidReverseAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidReverseAuction {

  import org.economicsl.auctions.singleunit.AuctionParticipant._

  implicit def reverseAuctionLikeOps[T <: Tradable]
                                    (a: OpenBidReverseAuction[T])
                                    : AuctionLike.Ops[T, OpenBidReverseAuction[T]] = {
    new AuctionLike.Ops[T, OpenBidReverseAuction[T]](a)
  }

  implicit def reverseAuctionLike[T <: Tradable]
                                 : AuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]] = {
    new AuctionLike[T, OpenBidReverseAuction[T]] with UniformPricing[T, OpenBidReverseAuction[T]] {
      protected def withOrderBook(a: OpenBidReverseAuction[T], orderBook: FourHeapOrderBook[T]): OpenBidReverseAuction[T] = {
        new OpenBidReverseAuction[T](orderBook, a.pricingPolicy, a.tickSize)
      }
    }
  }

  /** Create an instance of an "open-bid" reverse auction mechanism.
    *
    * @param reservation a `BidOrder` instance representing the reservation price for the reverse auction.
    * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `OpenBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return an `OpenBidReverseAuction` instance.
    */
  def withReservation[T <: Tradable]
                     (reservation: (Token, BidOrder[T]), pricingPolicy: PricingPolicy[T], tickSize: Currency)
                     : (OpenBidReverseAuction[T], Either[Rejected, Accepted]) = {
    val orderBook = FourHeapOrderBook.empty[T]
    val auction = new OpenBidReverseAuction[T](orderBook, pricingPolicy, tickSize)
    auction.insert(reservation)
  }

  /** Create a second-price, open-bid reverse auction (SPOBRA).
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `OpenBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return an `OpenBidReverseAuction` instance.
    * @note the winner of a SPOBRA is the seller who submitted the lowest priced ask order; however the winner receives
    *       an amount equal to the second lowest priced ask order.
    */
  def withAskQuotePricingPolicy[T <: Tradable](tickSize: Currency): OpenBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidReverseAuction[T](orderBook, new AskQuotePricingPolicy[T], tickSize)
  }

  /** Create a first-price, open-bid reverse auction (FPOBRA).
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `BidOrder` as well as all `AskOrder` instances submitted to the `OpenBidReverseAuction`
    *           must be for the same type of `Tradable`.
    * @return an `OpenBidReverseAuction` instance.
    * @note The winner of a FPOBRA is the seller who submitted the lowest priced ask order; the winner receives an
    *       amount equal to its own ask price.
    */
  def withBidQuotePricingPolicy[T <: Tradable](tickSize: Currency): OpenBidReverseAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidReverseAuction[T](orderBook, new BidQuotePricingPolicy[T], tickSize)
  }


}