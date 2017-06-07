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
import org.economicsl.auctions.multiunit.orders.{AskOrder, BidOrder}


protected[orderbooks] final case class UnMatchedOrders[K, T <: Tradable](askOrders: SortedAskOrders[K, T],
                                                                         bidOrders: SortedBidOrders[K, T]) {

  /* Limit price of "best" `BidOrder` instance must not exceed the limit price of the "best" `AskOrder` instance. */
  require(bidOrders.headOption.forall{ case (_, bidOrder) => askOrders.headOption.forall{ case (_, askOrder) => bidOrder.limit <= askOrder.limit } })

  def + (key: K, order: AskOrder[T]): UnMatchedOrders[K, T] = {
    new UnMatchedOrders(askOrders + (key, order), bidOrders)
  }

  def + (key: K, order: BidOrder[T]): UnMatchedOrders[K, T] = {
    new UnMatchedOrders(askOrders, bidOrders + (key, order))
  }

  /** Remove a collection of order from the collection of unmatched orders. */
  def - (key: K): UnMatchedOrders[K, T] = {
    if (askOrders.contains(key)) {
      new UnMatchedOrders(askOrders - key, bidOrders)
    } else {
      new UnMatchedOrders(askOrders, bidOrders - key)
    }
  }

  def - (key: K, order: AskOrder[T]): UnMatchedOrders[K, T] = {
    val remainingAskOrders = askOrders - (key, order)
    UnMatchedOrders(remainingAskOrders, bidOrders)
  }

  def - (key: K, order: BidOrder[T]): UnMatchedOrders[K, T] = {
    val remainingBidOrders = bidOrders - (key, order)
    UnMatchedOrders(askOrders, remainingBidOrders)
  }

  /** Check whether an order is contained in the collection of unmatched orders using. */
  def contains(key: K): Boolean = askOrders.contains(key) || bidOrders.contains(key)

  def isEmpty: Boolean = askOrders.isEmpty && bidOrders.isEmpty

  def nonEmpty: Boolean = askOrders.nonEmpty || bidOrders.nonEmpty

  def mergeWith(other: UnMatchedOrders[K, T]): UnMatchedOrders[K, T] = {
    new UnMatchedOrders(askOrders.mergeWith(other.askOrders), bidOrders.mergeWith(bidOrders))
  }

}


object UnMatchedOrders {

  /** Create an instance of `UnMatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty[K, T <: Tradable](implicit askOrdering: Ordering[K], bidOrdering: Ordering[K]): UnMatchedOrders[K, T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
