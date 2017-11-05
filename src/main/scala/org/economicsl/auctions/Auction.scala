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


/** Base trait for all `Auction` implementations.
  *
  * @tparam T
  * @tparam O
  */
trait Auction[T <: Tradable, O <: Order[T]]
  extends OrderReferenceIdGenerator[T]
  with Timestamper {

  /** Unique identifier for an `Auction`. */
  def auctionId: AuctionId

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param orderRefId the unique identifier for the order that should be removed.
    * @return
    */
  def cancel(orderRefId: OrderReferenceId): (Auction[T, O], Option[Canceled])

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return
    */
  def clear: (Auction[T, O], Option[Stream[SpotContract]])

  /** Combines and `Auction` mechanism with some other `Auction`.
    *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: Auction[T, O]): Auction[T, O]

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return
    */
  def insert(kv: (OrderId, O)): (Auction[T, O], Either[Rejected, Accepted])

  def orderBook: FourHeapOrderBook[T]

  def pricingPolicy: PricingPolicy[T]

  /** An `Auction` must have some protocol that contains all relevant information about auction. */
  def protocol: AuctionProtocol[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new pricing policy. */
  def withPricingPolicy(updated: PricingPolicy[T]): Auction[T, O]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
  def withProtocol(updated: AuctionProtocol[T]): Auction[T, O]

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: FourHeapOrderBook[T]): Auction[T, O]

}

