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
  * @param askOrders
  * @param bidOrders
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  require(askOrders.numberUnits == bidOrders.numberUnits)  // number of units must be the same!
  require(bidOrders.headOption.forall(bidOrder => askOrders.headOption.forall(askOrder => bidOrder.limit >= askOrder.limit)))  // value of lowest bid must exceed value of highest ask!

  protected[orderbooks] def + (orders: (AskOrder[T], BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders + orders._1, bidOrders + orders._2)
  }

  protected[orderbooks] def - (orders: (AskOrder[T], BidOrder[T])): MatchedOrders[T] = {
    new MatchedOrders(askOrders - orders._1, bidOrders - orders._2)
  }

  val askOrdering: Ordering[AskOrder[T]] = askOrders.ordering

  val bidOrdering: Ordering[BidOrder[T]] = bidOrders.ordering

  def contains(order: AskOrder[T]): Boolean = askOrders.contains(order)

  def contains(order: BidOrder[T]): Boolean = bidOrders.contains(order)

  def replace(existing: AskOrder[T], incoming: AskOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders - existing + incoming, bidOrders)
  }

  def replace(existing: BidOrder[T], incoming: BidOrder[T]): MatchedOrders[T] = {
    new MatchedOrders(askOrders, bidOrders - existing + incoming)
  }

  def takeBestMatch: (Option[(AskOrder[T], BidOrder[T])], MatchedOrders[T]) = {
    (askOrders.headOption, bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) =>
        (Some((askOrder, bidOrder)), new MatchedOrders(askOrders - askOrder, bidOrders - bidOrder))
      case _ => (None, this)
    }
  }

  def zipped: Stream[(AskOrder[T], BidOrder[T])] = {
    @annotation.tailrec
    def loop(askOrders: SortedAskOrders[T], bidOrders: SortedBidOrders[T], pairedOrders: Stream[(AskOrder[T], BidOrder[T])]): Stream[(AskOrder[T], BidOrder[T])] = {
      (askOrders.headOption, bidOrders.headOption) match {
        case (Some(askOrder), Some(bidOrder)) =>
          loop(askOrders.tail, bidOrders.tail, (askOrder, bidOrder) #:: pairedOrders)
        case _ => pairedOrders
      }
    }
    loop(askOrders, bidOrders, Stream.empty[(AskOrder[T], BidOrder[T])])
  }

}


object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[AskOrder[T]], bidOrdering: Ordering[BidOrder[T]]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
