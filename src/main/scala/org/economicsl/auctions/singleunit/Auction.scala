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


abstract class Auction[T <: Tradable, A <: Auction[T, A]](
  protected val orderBook: FourHeapOrderBook[T],
  protected val pricingPolicy: PricingPolicy[T],
  val tickSize: Currency)
    extends ReferenceGenerator {
  this: A =>

  import AuctionParticipant._

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param reference the unique identifier for the order that should be removed.
    * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    *         except the `order`.
    */
  def cancel(reference: Reference): (A, Option[Canceled]) = {
    val (residualOrderBook, kv) = orderBook.remove(reference)
    kv match {
      case Some((token, removedOrder)) =>
        val canceled = Canceled(removedOrder.issuer, token)
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
  def clear: (A, Option[Stream[Fill]])

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
      val reason = InvalidTickSize(order, tickSize)
      val rejected = Rejected(order.issuer, token, reason)
      (this, Left(rejected))
    } else {
      val reference = randomReference()
      val accepted = Accepted(order.issuer, reference)
      val updatedOrderBook = orderBook.insert(reference -> kv)
      (withOrderBook(updatedOrderBook), Right(accepted))
    }
  }

  protected def withOrderBook(updated: FourHeapOrderBook[T]): A

}

