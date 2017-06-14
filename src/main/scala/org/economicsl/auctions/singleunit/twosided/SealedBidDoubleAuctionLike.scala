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

import org.economicsl.auctions.{ClearResult, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.SealedBidAuctionLike
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.reverse.SealedBidReverseAuctionLike

import scala.util.Try


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

  class Ops[T <: Tradable, A <: { def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: SealedBidDoubleAuctionLike[T, A]) {

    /** Create a new instance of type class `A` whose order book contains an additional `AskOrder`.
      *
      * @param order the `AskOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
      */
    def insert(order: AskOrder[T]): Try[A] = ev.insert(a, order)

    /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
      *
      * @param order the `BidOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances.
      */
    def insert(order: BidOrder[T]): Try[A] = ev.insert(a, order)

    /** Create a new instance of type class `A` whose order book contains all previously submitted `AskOrder` and
      * `BidOrder` instances except the `order`.
      *
      * @param order the `AskOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` and
      *         `BidOrder` instances except the `order`.
      */
    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
      * except the `order`.
      *
      * @param order the `BidOrder` that should be added to the `orderBook`.
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` and
      *         `BidOrder` instances except the `order`.
      */
    def remove(order: BidOrder[T]): A = ev.remove(a, order)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[A] = ev.clear(a)

  }

}