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

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


private[orderbooks] class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T],
                                                               val bidOrders: SortedBidOrders[T]) {

  require(askOrders.numberUnits == bidOrders.numberUnits)
  require(bidOrders.headOption.forall(bidOrder => askOrders.headOption.forall(askOrder => bidOrder.value >= askOrder.value)))  // value of lowest bid must exceed value of highest ask!

  def - (orders: ((UUID, LimitAskOrder[T]), (UUID, LimitBidOrder[T]))): MatchedOrders[T] = {
    val ((uuid1, _), (uuid2, _)) = orders
    new MatchedOrders(askOrders - uuid1, bidOrders - uuid2)
  }

  val askOrdering: Ordering[LimitAskOrder[T]] = askOrders.ordering

  val bidOrdering: Ordering[LimitBidOrder[T]] = bidOrders.ordering

  def contains(uuid: UUID): Boolean = askOrders.contains(uuid) || bidOrders.contains(uuid)

  def replace(existing: (UUID, LimitAskOrder[T]), incoming: (UUID, LimitAskOrder[T])): MatchedOrders[T] = {
    val updatedAskOrders = (askOrders - existing._1).updated(incoming._1, incoming._2)
    new MatchedOrders(updatedAskOrders, bidOrders)
  }

  def replace(existing: (UUID, LimitBidOrder[T]), incoming: (UUID, LimitBidOrder[T])): MatchedOrders[T] = {
    val updatedBidOrders = (bidOrders - existing._1).updated(incoming._1, incoming._2)
    new MatchedOrders(askOrders, updatedBidOrders)
  }

  def updated(askOrder: (UUID, LimitAskOrder[T]), bidOrder: (UUID, LimitBidOrder[T])): MatchedOrders[T] = {
    val (uuid1, order1) = askOrder; val (uuid2, order2) = bidOrder
    new MatchedOrders(askOrders.updated(uuid1, order1), bidOrders.updated(uuid2, order2))
  }

  def zipped: Stream[(LimitAskOrder[T], LimitBidOrder[T])] = {
    ???
  }

}


private[orderbooks] object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
