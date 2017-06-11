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


/** Class for storing sets of matched `AskOrder` and `BidOrder` instances.
  *
  * @param askOrders a heap of `AskOrder` instances that have been matched with the `BidOrder` instances in `bidOrders`
  * @param bidOrders a heap of `BidOrder` instances that have been matched with `AskOrder` instances in `askOrders`.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `MatchedOrders` heap should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  /* Number of units supplied must equal the number of units demanded. */
  require(askOrders.numberUnits == bidOrders.numberUnits)

  /* Limit price of the first `BidOrder` must exceed the limit price of the first `AskOrder`. */
  require(bidOrders.headOption.forall(bidOrder => askOrders.headOption.forall(askOrder => bidOrder.limit >= askOrder.limit)))  // value of lowest bid must exceed value of highest ask!

  /** The ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance. */
  val askOrdering: Ordering[AskOrder[T]] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `MatchedOrders` instance. */
  val bidOrdering: Ordering[BidOrder[T]] = bidOrders.ordering

  /** Create a new `MatchedOrders` instance containing a matched pair of `(AskOrder, BidOrder)` instances.
    *
    * @param orders a matched pair of `(AskOrder, BidOrder)` instances that should be added.
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance and that also contains the matched pair of  `orders`.
    */
  def + (orders: (AskOrder[T], BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders + orders._1, bidOrders + orders._2)
  }

  /** Create a new `MatchedOrders` instance with the given matched pair of `(AskOrder, BidOrder)` removed.
    *
    * @param orders a matched pair of `(AskOrder, BidOrder)` instances that should be removed.
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance but that does not contain the matched pair of  `orders`.
    */
  def - (orders: (AskOrder[T], BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders - orders._1, bidOrders - orders._2)
  }

  /** Tests whether some `AskOrder` instance is contained in this `MatchedOrders` instance.
    *
    * @param order the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `MatchedOrders` instance; `false` otherwise.
    */
  def contains(order: AskOrder[T]): Boolean = askOrders.contains(order)

  /** Tests whether some `BidOrder` instance is contained in this `MatchedOrders` instance.
    *
    * @param order the `BidOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `MatchedOrders` instance; `false` otherwise.
    */
  def contains(order: BidOrder[T]): Boolean = bidOrders.contains(order)

  /** Replace an existing `AskOrder` instance with another `AskOrder` instance.
    *
    * @param existing the `AskOrder` instance that should be removed.
    * @param incoming the `AskOrder` instance that should be added.
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` except the `existing` `AskOrder`
    *         instance and that also contains the `incoming` `AskOrder` instance.
    */
  def replace(existing: AskOrder[T], incoming: AskOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders - existing + incoming, bidOrders)
  }

  /** Replace an existing `BidOrder` instance with another `BidOrder` instance.
    *
    * @param existing the `BidOrder` instance that should be removed.
    * @param incoming the `BidOrder` instance that should be added.
    * @return a new `MatchedOrders` instance that contains all of the `BidOrder` except the `existing` `BidOrder`
    *         instance and that also contains the `incoming` `BidOrder` instance.
    */
  def replace(existing: BidOrder[T], incoming: BidOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders, bidOrders - existing + incoming)
  }

  /** Split this `MatchedOrders` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `MatchedOrders` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if this
    *         `MatchedOrders` instance is non empty (first element is `None` otherwise), and whose second element is
    *         the residual `MatchedOrders` instance.
    */
  def splitAtBestMatch: (Option[(AskOrder[T], BidOrder[T])], MatchedOrders[T]) = {
    (askOrders.headOption, bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) =>
        (Some((askOrder, bidOrder)), new MatchedOrders(askOrders - askOrder, bidOrders - bidOrder))
      case _ => (None, this)
    }
  }

}


/** Companion object for `MatchedOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @param askOrdering ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance.
    * @param bidOrdering ordering used to sort the `BidOrder` instances contained in this `MatchedOrders` instance.
    * @tparam T all `AskOrder` and `BidOrder` instances stored in this `MatchedOrders` heap should be for the same
    *           type of `Tradable`.
    * @return an instance of `MatchedOrders`.
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[AskOrder[T]], bidOrdering: Ordering[BidOrder[T]]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
