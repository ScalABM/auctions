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

import org.economicsl.auctions.{ClearResult, Reference, Token}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.SealedBidAuctionLike
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.reverse.SealedBidReverseAuctionLike
import org.economicsl.core.Tradable


/** Base trait defining "sealed-bid double auction-like" behavior.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which sealed-bid double auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait SealedBidDoubleAuctionLike[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }]
    extends SealedBidAuctionLike[T, A]
    with SealedBidReverseAuctionLike[T, A]


/** Companion object for the `SealedBidDoubleAuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object SealedBidDoubleAuctionLike {

  class Ops[T <: Tradable, A <: { def tickSize: Long; def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: SealedBidDoubleAuctionLike[T, A]) {

    import org.economicsl.auctions.AuctionParticipant._

    /** Create a new instance of type class `A` whose order book contains all previously submitted `AskOrder` and
      * `BidOrder` instances except the `order`.
      *
      * @param reference
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` and
      *         `BidOrder` instances except the `order`.
      */
    def cancel(reference: Reference): (A, Option[Canceled]) = ev.cancel(a, reference)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[A] = ev.clear(a)

    /** Create a new instance of type class `A` whose order book contains an additional `AskOrder`.
      *
      * @param kv
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
      */
    def insert(kv: (Token, AskOrder[T])): (A, Either[Rejected, Accepted]) = ev.insert(a, kv)

    /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
      *
      * @param kv
      * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances.
      */
    def insert(kv: (Token, BidOrder[T])): (A, Either[Rejected, Accepted]) = ev.insert(a, kv)

  }

}