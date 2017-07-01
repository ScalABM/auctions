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

import org.economicsl.auctions.Reference
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.core.Tradable


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

  require(notCrossed, "Limit price of best `BidOrder` must not exceed the limit price of the best `AskOrder`.")

  /** The ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance. */
  val askOrdering: Ordering[(Reference, AskOrder[T])] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance. */
  val bidOrdering: Ordering[(Reference, BidOrder[T])] = bidOrders.ordering

  /** Create a new `UnMatchedOrders` instance containing the additional `AskOrder`.
    *
    * @param kv
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (Reference, Order[T])): UnMatchedOrders[T] = kv match {
    case askOrder@(_, _: AskOrder[T]) =>
      new UnMatchedOrders(askOrders + askOrder, bidOrders)
    case bidOrder@(_, _: BidOrder[T]) =>
      new UnMatchedOrders(askOrders, bidOrders + bidOrder)
  }

  /** Create a new `UnMatchedOrders` instance with the given `AskOrder` removed from this `UnMatchedOrders` instance.
    *
    * @param reference
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def - (reference: Reference): UnMatchedOrders[T] = {
    if (askOrders.contains(reference)) {
      new UnMatchedOrders(askOrders - reference, bidOrders)
    } else if (bidOrders.contains(reference)) {
      new UnMatchedOrders(askOrders, bidOrders - reference)
    } else {
      this
    }
  }

  /** Tests whether some `AskOrder` instance is contained in this `UnMatchedOrders` instance.
    *
    * @param reference the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `UnMatchedOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = askOrders.contains(reference) || bidOrders.contains(reference)

  /* Used to check the invariant that must hold for all `UnMatchedOrders` instances. */
  private[this] def notCrossed: Boolean = {
    bidOrders.headOption.forall{ case (_, bidOrder) =>
      askOrders.headOption.forall{ case (_, askOrder) =>
        bidOrder.limit <= askOrder.limit
      }
    }
  }

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
  def empty[T <: Tradable](askOrdering: Ordering[(Reference, AskOrder[T])],
                           bidOrdering: Ordering[(Reference, BidOrder[T])]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
