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
package org.economicsl.auctions.singleunit.orderbooks

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}


/** Class for storing sets of unmatched `AskOrder` and `BidOrder` instances.
  *
  * @param askOrders a heap of `AskOrder` instances.
  * @param bidOrders a heap of `BidOrder` instances.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class UnMatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  /* Limit price of "best" `BidOrder` instance must not exceed the limit price of the "best" `AskOrder` instance. */
  require(bidOrders.headOption.forall(bidOrder => askOrders.headOption.forall(askOrder => bidOrder.limit <= askOrder.limit)))

  /** The ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance. */
  val askOrdering: Ordering[AskOrder[T]] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance. */
  val bidOrdering: Ordering[BidOrder[T]] = bidOrders.ordering

  /** Create a new `UnMatchedOrders` instance containing the additional `AskOrder`.
    *
    * @param order an `AskOrder` that should be added.
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (order: AskOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders + order, bidOrders)

  /** Create a new `UnMatchedOrders` instance containing the additional `BidOrder`.
    *
    * @param order an `BidOrder` that should be added.
    * @return a new `UnMatchedOrders` instance that contains all of the `BidOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (order: BidOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders, bidOrders + order)

  /** Create a new `UnMatchedOrders` instance with the given `AskOrder` removed from this `UnMatchedOrders` instance.
    *
    * @param order an `AskOrder` that should be removed.
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def - (order: AskOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders - order, bidOrders)

  /** Create a new `UnMatchedOrders` instance with the given `BidOrder` removed from this `UnMatchedOrders` instance.
    *
    * @param order an `BidOrder` that should be removed.
    * @return a new `UnMatchedOrders` instance that contains all of the `BidOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def - (order: BidOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders, bidOrders - order)

  /** Tests whether some `AskOrder` instance is contained in this `UnMatchedOrders` instance.
    *
    * @param order the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `UnMatchedOrders` instance; `false` otherwise.
    */
  def contains(order: AskOrder[T]): Boolean = askOrders.contains(order)

  /** Tests whether some `BidOrder` instance is contained in this `UnMatchedOrders` instance.
    *
    * @param order the `BidOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `UnMatchedOrders` instance; `false` otherwise.
    */
  def contains(order: BidOrder[T]): Boolean = bidOrders.contains(order)

}


/** Companion object for `UnMatchedOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object UnMatchedOrders {

  /** Create an instance of `UnMatchedOrders`.
    *
    * @param askOrdering ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance.
    * @param bidOrdering ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance.
    * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
    *           type of `Tradable`.
    * @return an instance of `UnMatchedOrders`.
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[AskOrder[T]], bidOrdering: Ordering[BidOrder[T]]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
