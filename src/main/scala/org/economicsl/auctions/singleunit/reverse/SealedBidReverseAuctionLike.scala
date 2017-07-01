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

import org.economicsl.auctions.{ClearResult, Reference, Token}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.core.Tradable


/** Base trait defining "reverse auction-like" behavior.
  *
  * @tparam T all `BidOrder` instances must be for the same type of `Tradable`.
  * @tparam A type `A` for which auction-like operations should be defined.
  * @author davidrpugh
  * @since 0.1.0
  */
trait SealedBidReverseAuctionLike[T <: Tradable, A <: { def tickSize: Long; def orderBook: FourHeapOrderBook[T] }] {

  import org.economicsl.auctions.AuctionParticipant._

  /** Remove a previously accepted `AskOrder` instance from the `orderBook`.
    *
    * @param a an instance of type class `A`.
    * @param reference the unique identifier for the `AskOrder` that should be removed from the `orderBook`.
    * @return a `Tuple` whose first element is a `Canceled` instance encapsulating information about `AskOrder` that
    *         has been removed from the `orderBook` and whose second element is an instance of type class `A` whose
    *         `orderBook` contains all previously submitted `AskOrder` instances except the `AskOrder` corresponding
    *         to the `reference` identifier.
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
    * @param a an instance of type class `A`.
    * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
    *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
    *         `AskOrder` and `BidOrder` instances.
    */
  def clear(a: A): ClearResult[A]

  /** Attempt to insert an new `AskOrder` into the `orderBook`.
    *
    * @param a an instance of type class `A`.
    * @param kv
    * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
    */
  def insert(a: A, kv: (Token, AskOrder[T])): (A, Either[Rejected, Accepted]) = {
    val (_, order) = kv
    if (order.limit.value % a.tickSize > 0) {
      val reason = s"Limit price of ${order.limit} is not a multiple of the tick size ${a.tickSize}"
      val rejected = Rejected(order.issuer, reason)
      (a, Left(rejected))
    } else {
      val reference: Reference = ???
      val accepted = Accepted(order.issuer, reference)
      val updatedOrderBook = a.orderBook.insert(reference -> kv)
      (withOrderBook(a, updatedOrderBook), Right(accepted))
    }
  }

  protected def withOrderBook(a: A, orderBook: FourHeapOrderBook[T]): A

}


object SealedBidReverseAuctionLike {

  class Ops[T <: Tradable, A <: { def tickSize: Long; def orderBook: FourHeapOrderBook[T] }](a: A)(implicit ev: SealedBidReverseAuctionLike[T, A]) {

    import org.economicsl.auctions.AuctionParticipant._

    /** Remove a previously accepted `AskOrder` instance from the `orderBook`.
      *
      * @param reference the unique identifier for the `AskOrder` that should be removed from the `orderBook`.
      * @return a `Tuple` whose first element is a `Canceled` instance encapsulating information about `AskOrder` that
      *         has been removed from the `orderBook` and whose second element is an instance of type class `A` whose
      *         `orderBook` contains all previously submitted `AskOrder` instances except the `AskOrder` corresponding
      *         to the `reference` identifier.
      */
    def cancel(reference: Reference): (A, Option[Canceled]) = ev.cancel(a, reference)

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
      *
      * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
      *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
      *         `AskOrder` and `BidOrder` instances.
      */
    def clear: ClearResult[A] = ev.clear(a)

    /** Attempt to insert an new `AskOrder` into the `orderBook`.
      *
      * @param kv
      * @return an instance of type class `A` whose order book contains all previously submitted `AskOrder` instances.
      */
    def insert(kv: (Token, AskOrder[T])): (A, Either[Rejected, Accepted]) = ev.insert(a, kv)

  }

}