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
import org.economicsl.auctions.messages._
import org.economicsl.core.Tradable


/** Base trait for all auction implementations.
  *
  * @tparam T
  * @note Note the use of F-bounded polymorphism over Type classes. We developed an alternative implementation using the
  *       Type class pattern that was quite elegant, however Type classes can not be used directly from Java. In order
  *       to use the Type class implementation from Java, we would need to develop (and maintain!) separate wrappers for
  *       each auction implementation.
  */
trait SingleUnitAuction[T <: Tradable]
  extends Auction[T, Order[T] with SingleUnit[T]] {

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param orderRefId the unique identifier for the order that should be removed.
    * @return
    */
  def cancel(orderRefId: OrderReferenceId): (SingleUnitAuction[T], Option[Canceled]) = {
    val (residualOrderBook, removedOrder) = orderBook.remove(orderRefId)
    removedOrder match {
    case Some((orderId, order)) =>
      val timestamp = currentTimeMillis()  // todo not sure that we want to use real time for timestamps!
      val canceled = CanceledByIssuer(order, orderId, auctionId, timestamp)
      (withOrderBook(residualOrderBook), Some(canceled))
    case None =>
      (this, None)
    }
  }

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return
    */
  def clear: (SingleUnitAuction[T], Option[Stream[SpotContract]])

  /** Combines and `Auction` mechanism with some other `Auction`.
    *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: SingleUnitAuction[T]): SingleUnitAuction[T]

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
    *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
    *         instances.
    */
  def insert(kv: (OrderId, Order[T] with SingleUnit[T])): (SingleUnitAuction[T], Either[Rejected, Accepted]) = kv match {
    case (orderId, order) if order.limit.value % protocol.tickSize > 0 =>
      val timestamp = currentTimeMillis()  // todo not sure that we want to use real time for timestamps!
      val reason = InvalidTickSize(order, protocol)
      val rejected = Rejected(timestamp, orderId, order, reason)
      (this, Left(rejected))
    case (orderId, order) if !order.tradable.equals(protocol.tradable) =>
      val timestamp = currentTimeMillis()  // todo not sure that we want to use real time for timestamps!
      val reason = InvalidTradable(order, protocol)
      val rejected = Rejected(timestamp, orderId, order, reason)
      (this, Left(rejected))
    case (orderId, order) =>
      val orderRefId = randomOrderReferenceId() // todo would prefer that these not be randomly generated!
      val timestamp = currentTimeMillis()  // todo not sure that we want to use real time for timestamps!
      val accepted = Accepted(order, orderId, orderRefId, auctionId, timestamp)
      val updatedOrderBook = orderBook.insert(orderRefId -> kv)
      (withOrderBook(updatedOrderBook), Right(accepted))
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: FourHeapOrderBook[T]): SingleUnitAuction[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new pricing policy. */
  def withPricingPolicy(updated: PricingPolicy[T]): SingleUnitAuction[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
  def withProtocol(updated: AuctionProtocol[T]): SingleUnitAuction[T]

}

