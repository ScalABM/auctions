/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions.multiunit.orderbooks

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.multiunit.orders.{AskOrder, BidOrder}


protected[orderbooks] final case class MatchedOrders[K, T <: Tradable](askOrders: SortedAskOrders[K, T],
                                                                       bidOrders: SortedBidOrders[K, T]) {

  /* Number of units supplied must equal the number of units demanded. */
  require(askOrders.numberUnits == bidOrders.numberUnits)

  /* Limit price of the first `BidOrder` must exceed the limit price of the first `AskOrder`. */
  require(bidOrders.headOption.forall{ case (_, bidOrder) => askOrders.headOption.forall{ case (_, askOrder) => bidOrder.limit >= askOrder.limit } })

  val askOrdering: Ordering[K] = askOrders.ordering

  val bidOrdering: Ordering[K] = bidOrders.ordering

  /** Add a new pair of ask and bid orders into the MatchedOrders.
    *
    * @note Unless the quantities of the `AskOrder` and `BidOrder` match exactly, then the larger order must
    *       be rationed in order to maintain the invariant that the total quantity of supply matches the total quantity
    *       of demand.
    */
  def + (kv1: (K, AskOrder[T]), kv2: (K, BidOrder[T])): (MatchedOrders[K, T], Option[UnMatchedOrders[K, T]]) = {
    val excessDemand = kv2._2.quantity - kv1._2.quantity
    if (excessDemand < Quantity(0)) {
      val (matched, rationed) = (kv1._2.withQuantity(kv2._2.quantity), kv1._2.withQuantity(-excessDemand)) // split the askOrder into a matched and rationed component
      val rationedOrders = UnMatchedOrders.empty[K, T](askOrders.ordering, bidOrders.ordering)
      (MatchedOrders(askOrders + (kv1._1, matched), bidOrders + (kv2._1, kv2._2)), Some(rationedOrders + (kv1._1, rationed)))
    } else if (excessDemand > Quantity(0)) {
      val (matched, rationed) = (kv2._2.withQuantity(kv1._2.quantity), kv2._2.withQuantity(excessDemand)) // split the bidOrder into a matched and residual component
      val rationedOrders = UnMatchedOrders.empty[K, T](askOrders.ordering, bidOrders.ordering)
      (MatchedOrders(askOrders + (kv1._1, kv1._2), bidOrders + (kv2._1, matched)), Some(rationedOrders + (kv2._1, rationed)))
    } else {
      (MatchedOrders(askOrders + (kv1._1, kv1._2), bidOrders + (kv2._1, kv2._2)), None)
    }
  }

  def - (key: K, order: AskOrder[T]): (MatchedOrders[K, T], UnMatchedOrders[K, T]) = {
    val remainingAskOrders = askOrders - (key, order)
    val (matched, unMatched) = bidOrders.splitAt(order.quantity)
    val empty = SortedAskOrders.empty[K, T](askOrdering)
    (MatchedOrders(remainingAskOrders, matched), UnMatchedOrders(empty, unMatched))
  }

  def - (key: K, order: BidOrder[T]): (MatchedOrders[K, T], UnMatchedOrders[K, T]) = {
    val remainingBidOrders = bidOrders - (key, order)
    val (matched, unMatched) = askOrders.splitAt(order.quantity)
    val empty = SortedBidOrders.empty[K, T](bidOrdering)
    (MatchedOrders(matched, remainingBidOrders), UnMatchedOrders(unMatched, empty))
  }

  def contains(key: K): Boolean = askOrders.contains(key) || bidOrders.contains(key)

  def isEmpty: Boolean = askOrders.isEmpty && bidOrders.isEmpty

  def nonEmpty: Boolean = askOrders.nonEmpty && bidOrders.nonEmpty

  /*
  def removeAndReplace(askOrder: (UUID, AskOrder[T]), bidOrder: (UUID, BidOrder[T])): MatchedOrders[K, T] = {
    new MatchedOrders(askOrders - askOrder._1, bidOrders.updated(bidOrder._1, bidOrder._2))
  }

  def updated(askOrder: (UUID, AskOrder[T]), bidOrder: (UUID, BidOrder[T])): MatchedOrders[K, T] = {
    new MatchedOrders(askOrders + askOrder, bidOrders + bidOrder)
  }
  */

}


object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[K, T <: Tradable](askOrdering: Ordering[K], bidOrdering: Ordering[K]): MatchedOrders[K, T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
