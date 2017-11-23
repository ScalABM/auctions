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
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.SingleUnitPricingPolicy
import org.economicsl.core.util.Timestamper
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
    extends GenSingleUnitAuction[T]
    with SenderIdGenerator
    with Timestamper {

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
    *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
    *         `AskOrder` and `BidOrder` instances.
    */
  def clear: (A, Option[Stream[SpotContract]])

  /** Combines and `Auction` mechanism with some other `Auction`.
  *
    * @param that
    * @return
    * @note this method is necessary in order to parallelize auction simulations.
    */
  def combineWith(that: A): A = {
    require(protocol.tradable.equals(that.protocol.tradable), "Only auctions for the same Tradable can be combined!")
    val combinedOrderBooks = orderBook.combineWith(that.orderBook)
    val withCombinedOrderBooks = withOrderBook(combinedOrderBooks)
    val combinedTickSize = leastCommonMultiple(protocol.tickSize, that.protocol.tickSize)
    val updatedProtocol = protocol.withTickSize(combinedTickSize)
    withCombinedOrderBooks.withProtocol(updatedProtocol)
  }

  /** An `Auction` must have some protocol that contains all relevant information about auction. */
  def protocol: AuctionProtocol[T]

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new pricing policy. */
  def withPricingPolicy(updated: SingleUnitPricingPolicy[T]): A

  /** Returns an auction of type `A` that encapsulates the current auction state but with a new protocol. */
  def withProtocol(updated: AuctionProtocol[T]): A

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOrderBook(updated: FourHeapOrderBook[T]): A

  protected val orderBook: FourHeapOrderBook[T]

  protected val pricingPolicy: SingleUnitPricingPolicy[T]

  /** Computest the least common multiple of two tick sizes. */
  private[this] def leastCommonMultiple(a: Long, b: Long) = {

    @annotation.tailrec
    def gcd(a: Long, b: Long): Long = {
      if (b == 0) a.abs else gcd(b, a % b)
    }

    (a.abs / gcd(a,b)) * (b.abs / gcd(a, b))  // todo check for overflow?

  }

}

