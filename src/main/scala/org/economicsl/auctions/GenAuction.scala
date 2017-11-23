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
package org.economicsl.auctions

import org.economicsl.auctions.messages._
import org.economicsl.core.util.Timestamper
import org.economicsl.core.Tradable


/** Base trait for all auction implementations.
  *
  * @tparam T
  * @tparam O
  * @tparam A
  * @note Note the use of F-bounded polymorphism over Type classes. I developed an alternative implementation using the
  *       Type class pattern that was quite elegant, however Type classes can not be used directly from Java. In order
  *       to use the Type class implementation from Java, we would need to develop (and maintain!) separate wrappers for
  *       each auction implementation.
  * @author davidrpugh
  * @since 0.1.0
  */
trait GenAuction[T <: Tradable, -O <: NewOrder[T] with PriceQuantitySchedule[T], OB <: OrderBook[T, O, OB], A <: GenAuction[T, O, OB, A]]
    extends OrderReferenceIdGenerator[T, O, A]
    with Timestamper {
  this: A =>

  def auctionId: AuctionId

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param message the unique identifier for the order that should be removed.
    * @return
    */
  def cancel(message: CancelOrder): (A, Either[CancelOrderRejected, CancelOrderAccepted]) = {
    val (residualOrderBook, removedOrder) = orderBook - message.orderRefId
    removedOrder match {
      case Some((orderId, _)) =>
        val timestamp = currentTimeMillis()
        val accepted = CancelOrderAccepted(orderId, auctionId, timestamp)
        (withOrderBook(residualOrderBook), Right(accepted))
      case None =>
        val reason = OrderNotFound
        val timestamp = currentTimeMillis()
        val rejected = CancelOrderRejected(message.orderId, reason, auctionId, timestamp)
        (this, Left(rejected))
    }
  }

  /** Calculate a clearing price(s) and remove all orders instances that are matched at that price(s).
    *
    * @return
    */
  def clear: (A, Option[Stream[SpotContract]])

  /** Combines and `Auction` mechanism with some other `Auction`.
    *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: A): A

  /** Create a new instance of auction type `A` whose order book contains an additional order of type `O`.
    *
    * @param message
    * @return
    */
  def insert(message: O): (A, Either[NewOrderRejected, NewOrderAccepted]) = {
    if (!message.tradable.equals(protocol.tradable)) {
      val timestamp = currentTimeMillis()
      val reason = InvalidTradable(message.tradable, protocol.tradable)
      val rejected = NewOrderRejected(message.orderId, reason, auctionId, timestamp)
      (this, Left(rejected))
    } else {
      val orderRefId = randomOrderReferenceId()
      val timestamp = currentTimeMillis()
      val accepted = NewOrderAccepted(message.orderId, orderRefId, auctionId, timestamp)
      val updatedOrderBook = orderBook + (orderRefId -> (message.orderId -> message))
      (withOrderBook(updatedOrderBook), Right(accepted))
    }
  }

  /** An `Auction` must have some protocol that contains all relevant information about auction. */
  def protocol: AuctionProtocol[T]

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: OB): A

  protected val orderBook: OB

  protected val pricingPolicy: PricingPolicy[T, O, OB]

}

