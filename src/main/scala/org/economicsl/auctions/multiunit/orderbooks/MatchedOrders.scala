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

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


private[orderbooks] class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  require(askOrders.numberUnits == bidOrders.numberUnits)  // number of units must be the same!
  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit >= askOrders.head.limit))  // value of lowest bid must exceed value of highest ask!

  def + (orders: (LimitAskOrder[T], LimitBidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders + orders._1, bidOrders + orders._2)
  }

  def - (orders: (LimitAskOrder[T], LimitBidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders - orders._1, bidOrders - orders._2)
  }

  val askOrdering: Ordering[LimitAskOrder[T]] = askOrders.ordering

  val bidOrdering: Ordering[LimitBidOrder[T]] = bidOrders.ordering

  def contains(order: LimitAskOrder[T]): Boolean = askOrders.contains(order)

  def contains(order: LimitBidOrder[T]): Boolean = bidOrders.contains(order)

  def replace(existing: LimitAskOrder[T], incoming: LimitAskOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders - existing + incoming, bidOrders)
  }

  def replace(existing: LimitBidOrder[T], incoming: LimitBidOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders, bidOrders - existing + incoming)
  }

  def zipped: Stream[(LimitAskOrder[T], LimitBidOrder[T])] = {
    @annotation.tailrec
    def loop(askOrders: SortedAskOrders[T], bidOrders: SortedBidOrders[T], pairedOrders: Stream[(LimitAskOrder[T], LimitBidOrder[T])]): Stream[(LimitAskOrder[T], LimitBidOrder[T])] = {
      if (askOrders.isEmpty || bidOrders.isEmpty) {
        pairedOrders
      } else {
        val pair = (askOrders.head, bidOrders.head)
        loop(askOrders.tail, bidOrders.tail, Stream.cons(pair, pairedOrders))
      }
    }
    loop(askOrders, bidOrders, Stream.empty[(LimitAskOrder[T], LimitBidOrder[T])])
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
