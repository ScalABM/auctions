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
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


private[orderbooks] class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T],
                                                               val bidOrders: SortedBidOrders[T]) {

  require(askOrders.quantity == bidOrders.quantity)
  require(bidOrders.headOption.forall{ case (_, bidOrder) => askOrders.headOption.forall{ case (_, askOrder) => bidOrder.value >= askOrder.value }})  // value of lowest bid must exceed value of highest ask!

  def - (uuid: UUID): (MatchedOrders[T], Option[SortedAskOrders[T]], Option[SortedBidOrders[T]]) = {
    if (askOrders.contains(uuid)) {
      val removedOrder = askOrders(uuid)
      val (matched, residual) = bidOrders.splitAt(removedOrder.quantity)
      (new MatchedOrders(askOrders - uuid, residual), None, Some(matched))
    } else {
      val removedOrder = bidOrders(uuid)
      val (matched, residual) = askOrders.splitAt(removedOrder.quantity)
      (new MatchedOrders(residual, bidOrders - uuid), Some(matched), None)
    }
  }

  val askOrdering: Ordering[(UUID, LimitAskOrder[T])] = askOrders.ordering

  val bidOrdering: Ordering[(UUID, LimitBidOrder[T])] = bidOrders.ordering

  def contains(uuid: UUID): Boolean = askOrders.contains(uuid) || bidOrders.contains(uuid)

  def removeAndReplace(askOrder: (UUID, LimitAskOrder[T]), bidOrder: (UUID, LimitBidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders - askOrder._1, bidOrders.updated(bidOrder._1, bidOrder._2))
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
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[(UUID, LimitAskOrder[T])], bidOrdering: Ordering[(UUID, LimitBidOrder[T])]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
