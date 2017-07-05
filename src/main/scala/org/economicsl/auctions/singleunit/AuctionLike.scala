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

import org.economicsl.auctions.{ClearResult, Reference, ReferenceGenerator, Token}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


/** Base trait defining "sealed-bid" auction-like behavior.
  *
  * @tparam T all `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which sealed-bid, auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait AuctionLike[T <: Tradable, A <: Auction[T]]
    extends ReferenceGenerator {

  import AuctionParticipant._

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param a an instance of type class `A`.
    * @param reference the unique identifier for the order that should be removed.
    * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    *         except the `order`.
    */
  def cancel(a: A, reference: Reference): (A, Option[Canceled]) = {
    val (residualOrderBook, kv) = a.orderBook.remove(reference)
    kv match {
      case Some((token, removedOrder)) =>
        val canceled = Canceled(removedOrder.issuer, token)
        (withOrderBook(a, residualOrderBook), Some(canceled))
      case None =>
        (a, None)
    }
  }

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @param a an instance of type `A`.
    * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
    *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
    *         `AskOrder` and `BidOrder` instances.
    */
  def clear(a: A): ClearResult[A]

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param a  an instance of type class `A`.
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
    *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
    *         instances.
    */
  def insert(a: A, kv: (Token, Order[T])): (A, Either[Rejected, Accepted]) = {
    val (_, order) = kv
    if (order.limit.value % a.tickSize > 0) {
      val reason = s"Limit price of ${order.limit} is not a multiple of the tick size ${a.tickSize}"
      val rejected = Rejected(order.issuer, reason)
      (a, Left(rejected))
    } else {
      val reference = randomReference()
      val accepted = Accepted(order.issuer, reference)
      val updatedOrderBook = a.orderBook.insert(reference -> kv)
      (withOrderBook(a, updatedOrderBook), Right(accepted))
    }
  }

  protected def withOrderBook(a: A, orderBook: FourHeapOrderBook[T]): A

}


/** Companion object for the `AuctionLike` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object AuctionLike {

  /** Class defining auction-like operations for an auction of type `A`.
    *
    * @param a an instance of type `A` for which "sealed-bid" auction-like operations should be defined.
    * @param ev an instance of a class that implements the `SealedBidAuctionLike` trait.
    * @tparam T all `BidOrder` instances must be for the same type of `Tradable`.
    * @tparam A the type class for which auction-like operations should be defined.
    * @note The `Ops` class instance takes as a parameter an instance of type `A` and implements the auction-like
    *       operations `insert`, `remove`, and `clear` by delegating these method calls to the implicit `ev` instance.
    */
  class Ops[T <: Tradable, A <: Auction[T]](a: A)(implicit ev: AuctionLike[T, A]) {

    import AuctionParticipant._

    /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
      * except the `order`.
      *
      * @param reference the unique identifier for the order that should be removed.
      * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
      *         except the `order`.
      */
    def cancel(reference: Reference): (A, Option[Canceled]) = ev.cancel(a, reference)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[A] = ev.clear(a)

    /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
      *
      * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
      *           entered into the `orderBook`.
      * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
      *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
      *         instances.
      */
    def insert(kv: (Token, Order[T])): (A, Either[Rejected, Accepted]) = ev.insert(a, kv)

  }

}
