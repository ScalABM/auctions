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

import java.util.UUID

import org.economicsl.auctions.{Quantity, Tradable}
import org.economicsl.auctions.multiunit.BidOrder
import org.economicsl.auctions.multiunit.orders.{AskOrder, BidOrder}


private[orderbooks] class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T],
                                                               val bidOrders: SortedBidOrders[T]) {

  /* Number of units supplied must equal the number of units demanded. */
  require(askOrders.numberUnits == bidOrders.numberUnits)

  /* Limit price of the first `BidOrder` must exceed the limit price of the first `AskOrder`. */
  require(bidOrders.headOption.forall{ case (_, bidOrder) => askOrders.headOption.forall{ case (_, askOrder) => bidOrder.limit >= askOrder.limit } })  // value of lowest bid must exceed value of highest ask!

  val isEmpty: Boolean = askOrders.isEmpty && bidOrders.isEmpty

  val nonEmpty: Boolean = askOrders.nonEmpty && bidOrders.nonEmpty

  /** Add a new pair of ask and bid orders into the MatchedOrders.
    * @note Unless the quantities of the `AskOrder` and `BidOrder` match exactly, then the larger order must
    *       be rationed in order to maintain the invariant that the total quantity of supply matches the total quantity
    *       of demand.
    */
  def + (kv1: (UUID, AskOrder[T]), kv2: (UUID, BidOrder[T])): (MatchedOrders[T], Option[UnMatchedOrders[T]]) = {
    val ((uuid1, askOrder), (uuid2, bidOrder)) = (kv1, kv2)
    val excessDemand = bidOrder.quantity - askOrder.quantity
    if (excessDemand < Quantity(0)) {
      val (matched, rationed) = (askOrder.withQuantity(bidOrder.quantity), askOrder.withQuantity(-excessDemand)) // split the askOrder into a matched and rationed component
      val rationedOrders = UnMatchedOrders.empty[T](askOrders.ordering, bidOrders.ordering)
      (new MatchedOrders(askOrders + (uuid1 -> matched), bidOrders + kv2), Some(rationedOrders + (uuid1, rationed)))
    } else if (excessDemand > Quantity(0)) {
      val (matched, rationed) = (bidOrder.withQuantity(askOrder.quantity), bidOrder.withQuantity(excessDemand)) // split the bidOrder into a matched and residual component
      val rationedOrders = UnMatchedOrders.empty[T](askOrders.ordering, bidOrders.ordering)
      (new MatchedOrders(askOrders + kv1, bidOrders + (uuid2 -> matched)), Some(rationedOrders + (uuid2, rationed)))
    } else {
      (new MatchedOrders(askOrders + kv1, bidOrders + kv2), None)
    }
  }

  def - (uuid: UUID): (MatchedOrders[T], UnMatchedOrders[T]) = {
    if (askOrders.contains(uuid)) {
      val removedOrder = askOrders(uuid)
      val (unMatched, residual) = bidOrders.splitAt(removedOrder.quantity)
      // (new MatchedOrders(askOrders - uuid, residual), UnMatchedOrders.withEmptyAskOrders(unMatched))
      ???
    } else {
      val removedOrder = bidOrders(uuid)
      val (unMatched, residual) = askOrders.splitAt(removedOrder.quantity)
      // (new MatchedOrders(residual, bidOrders - uuid), UnMatchedOrders.withEmptyBidOrders(unMatched))
      ???
    }
  }

  val askOrdering: Ordering[(UUID, AskOrder[T])] = askOrders.ordering

  val bidOrdering: Ordering[(UUID, BidOrder[T])] = bidOrders.ordering

  def contains(uuid: UUID): Boolean = askOrders.contains(uuid) || bidOrders.contains(uuid)

  def swap(uuid: UUID, order: AskOrder[T], existing: UUID): (MatchedOrders[T], UnMatchedOrders[T]) = {
    /*val askOrder = askOrders(existing)
    if (order.quantity > askOrder.quantity) {
      // if order.quantity > askOrder.quantity, then we need to split the order into matched and rationed components; remove the askOrder and add it to the unmatched orders along with the rationed component or order; finally add the matched component of order to the matched orders.
      val (matched, rationed) = (order.withQuantity(askOrder.quantity), order.withQuantity(residual))
      val residualAskOrders = askOrders - existing
      val empty = UnMatchedOrders.empty[T](???, ???)
      val unMatchedOrders = empty ++ ((existing, askOrder), (uuid, rationed))
      (new MatchedOrders(residualAskOrders + (uuid, matched), bidOrders), unMatchedOrders)
      ???
    } else if (order.quantity < askOrder.quantity) {
      // if order.quantity < askOrder.quantity, then we need to remove the askOrder and split it into matched and rationed components; add the order and the match component into the matched set and then add the rationed component of askOrder into the unMatched set.
      ???
    } else {
      // if quantities match then we just need to remove askOrder and add it to unmatched orders; then add order to matched orders
      val residualAskOrders = askOrders - existing
      (new MatchedOrders(residualAskOrders + (uuid -> order), bidOrders), UnMatchedOrders.withEmptyBidOrders(askOrder))
    }*/
    ???
  }

  def swap(uuid: UUID, order: BidOrder[T], existing: UUID): (MatchedOrders[T], UnMatchedOrders[T]) = {
    ???
  }

  def removeAndReplace(askOrder: (UUID, AskOrder[T]), bidOrder: (UUID, BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders - askOrder._1, bidOrders.updated(bidOrder._1, bidOrder._2))
  }

  def updated(askOrder: (UUID, AskOrder[T]), bidOrder: (UUID, BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders + askOrder, bidOrders + bidOrder)
  }

  def zipped: Stream[(AskOrder[T], BidOrder[T])] = {
    ???
  }

}


private[orderbooks] object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[(UUID, AskOrder[T])], bidOrdering: Ordering[(UUID, BidOrder[T])]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
