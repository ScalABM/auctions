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

import org.economicsl.auctions.Token
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy, UniformPricing}
import org.economicsl.auctions.singleunit.quoting.{AskPriceQuoting, AskPriceQuotingPolicy}
import org.economicsl.core.{Currency, Tradable}


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
class OpenBidAuction[T <: Tradable] private(
    protected[singleunit] val orderBook: FourHeapOrderBook[T],
    val pricingPolicy: PricingPolicy[T],
    val tickSize: Currency)
  extends Auction[T]
  with AskPriceQuoting[T]


  /** Companion object for the `OpenBidAuction` type class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OpenBidAuction {

  import AuctionParticipant._

  implicit def mkAuctionOps[T <: Tradable](a: OpenBidAuction[T]): AuctionLike.Ops[T, OpenBidAuction[T]] = {
    new AuctionLike.Ops[T, OpenBidAuction[T]](a)
  }

  implicit def mkAuctionLike[T <: Tradable]
                            : AuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] = {
    new AuctionLike[T, OpenBidAuction[T]] with UniformPricing[T, OpenBidAuction[T]] {
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
  def withReservation[T <: Tradable]
                     (reservation: (Token, AskOrder[T]), pricingPolicy: PricingPolicy[T], tickSize: Currency)
                     : (OpenBidAuction[T], Either[Rejected, Accepted]) = {
    val orderBook = FourHeapOrderBook.empty[T]
    val auction = new OpenBidAuction[T](orderBook, pricingPolicy, tickSize)
    auction.insert(reservation)
  }

  /** Create an instance of a first-price, open-bid auction (FPOBA) mechanism.
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return an `OpenBidAuction` instance.
    */
  def withAskQuotePricingPolicy[T <: Tradable](tickSize: Currency): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook, new AskQuotePricingPolicy[T], tickSize)
  }

  /** Create an instance of a second-price, open-bid auction (SPOBA) mechanism.
    *
    * @param tickSize the minimum price movement of a tradable.
    * @tparam T the reservation `AskOrder` as well as all `BidOrder` instances submitted to the `OpenBidAuction` must
    *           be for the same type of `Tradable`.
    * @return an `OpenBidAuction` instance.
    */
  def withBidQuotePricingPolicy[T <: Tradable](tickSize: Currency): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook, new BidQuotePricingPolicy[T], tickSize)
  }

}
