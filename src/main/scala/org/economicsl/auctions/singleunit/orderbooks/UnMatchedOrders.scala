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
package org.economicsl.auctions.singleunit.orderbooks

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.{LimitAskOrder, LimitBidOrder}


private[orderbooks] class UnMatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit <= askOrders.head.limit))

  def + (order: LimitAskOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders + order, bidOrders)

  def + (order: LimitBidOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders, bidOrders + order)

  def - (order: LimitAskOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders - order, bidOrders)

  def - (order: LimitBidOrder[T]): UnMatchedOrders[T] = new UnMatchedOrders(askOrders, bidOrders - order)

  val askOrdering: Ordering[LimitAskOrder[T]] = askOrders.ordering

  val bidOrdering: Ordering[LimitBidOrder[T]] = bidOrders.ordering

  def contains(order: LimitAskOrder[T]): Boolean = askOrders.contains(order)

  def contains(order: LimitBidOrder[T]): Boolean = bidOrders.contains(order)

}


private[orderbooks] object UnMatchedOrders {

  /** Create an instance of `UnMatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
