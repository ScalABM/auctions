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

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


/** Base trait for all auction implementations.
  *
  * @tparam T
  * @tparam A
  * @note Note the use of F-bounded polymorphism over Type classes. We developed an alternative implementation using the
  *       Type class pattern that was quite elegant, however Type classes can not be used directly from Java. In order
  *       to use the Type class implementation from Java, we would need to develop (and maintain!) separate wrappers.
  */
trait Auction[T <: Tradable, A <: Auction[T, A]]
    extends ReferenceGenerator
    with Timestamper {
  this: A =>

  import OrderTracking._

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param reference the unique identifier for the order that should be removed.
    * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    *         except the `order`.
    */
  def cancel(reference: Reference): (A, Option[Canceled]) = {
    val (residualOrderBook, removedOrder) = orderBook.remove(reference)
    removedOrder match {
      case Some((token, order)) =>
        val timestamp = currentTimeMillis()
        val canceled = Canceled(timestamp, token, order, ???)
        (withOrderBook(residualOrderBook), Some(canceled))
      case None =>
        (this, None)
    }
  }

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
    *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
    *         `AskOrder` and `BidOrder` instances.
    */
  def clear: (A, Option[Stream[SpotContract]])

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
    *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
    *         instances.
    */
  def insert(kv: (Token, Order[T])): (A, Either[Rejected, Accepted]) = {
    val (token, order) = kv
    if (order.limit.value % tickSize > 0) {
      val timestamp = currentTimeMillis()
      val reason = InvalidTickSize(order, tickSize)
      val rejected = Rejected(timestamp, token, order, reason)
      (this, Left(rejected))
    } else {
      val timestamp = currentTimeMillis()
      val reference = randomReference() // SIDE EFFECT !!!
      val accepted = Accepted(timestamp, token, order, reference)
      val updatedOrderBook = orderBook.insert(reference -> kv)
      (withOrderBook(updatedOrderBook), Right(accepted))
    }
  }

  def tickSize: Currency

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new pricing policy. */
  def withPricingPolicy(updated: PricingPolicy[T]): A

  /** Returns an auction of type `A` the encapsulates the current auction state but with a new tick size. */
  def withTickSize(updated: Currency): A

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: FourHeapOrderBook[T]): A

  protected val orderBook: FourHeapOrderBook[T]

  protected val pricingPolicy: PricingPolicy[T]

}

