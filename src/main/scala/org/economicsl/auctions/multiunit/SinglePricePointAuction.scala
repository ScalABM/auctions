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
package org.economicsl.auctions.multiunit

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
trait SinglePricePointAuction[T <: Tradable]
  extends Auction[T, Order[T] with SinglePricePoint[T]] {

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param orderRefId the unique identifier for the order that should be removed.
    * @return
    */
  def cancel(orderRefId: OrderReferenceId): (SinglePricePointAuction[T], Option[Canceled])

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return
    */
  def clear: (SinglePricePointAuction[T], Option[Stream[SpotContract]])

  /** Combines and `Auction` mechanism with some other `Auction`.
    *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: SinglePricePointAuction[T]): SinglePricePointAuction[T]

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
    *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
    *         instances.
    */
  def insert(kv: (OrderId, Order[T] with SinglePricePoint[T])): (SinglePricePointAuction[T], Either[Rejected, Accepted])

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: FourHeapOrderBook[T]): SinglePricePointAuction[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new pricing policy. */
  def withPricingPolicy(updated: PricingPolicy[T]): SinglePricePointAuction[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
  def withProtocol(updated: AuctionProtocol[T]): SinglePricePointAuction[T]

}

