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
package org.economicsl.auctions.orderbooks

import org.economicsl.auctions._

import scala.collection.immutable.TreeSet


private[orderbooks] class MatchedOrders[T <: Tradable, A <: LimitAskOrder[T], B <: LimitBidOrder[T]] private(val askOrders: SortedAskOrders[T, A], val bidOrders: SortedBidOrders[T, B]) {

  require(askOrders.size == bidOrders.size)  // number of units must be the same!
  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit >= askOrders.head.limit))  // value of lowest bid must exceed value of highest ask!

  def + (orders: (A, B)): MatchedOrders[T, A, B] = {
    new MatchedOrders(askOrders + orders._1, bidOrders + orders._2)
  }

  def - (orders: (A, B)): MatchedOrders[T, A, B] = {
    new MatchedOrders(askOrders - orders._1, bidOrders - orders._2)
  }

  def contains(order: A): Boolean = askOrders.contains(order)

  def contains(order: B): Boolean = bidOrders.contains(order)

  def replace(existing: A, incoming: A): MatchedOrders[T, A, B] = {
    new MatchedOrders(askOrders - existing + incoming, bidOrders)
  }

  def replace(existing: B, incoming: B): MatchedOrders[T, A, B] = {
    new MatchedOrders(askOrders, bidOrders - existing + incoming)
  }

  def zipped: Stream[(A, B)] = {
    @annotation.tailrec
    def loop(askOrders: SortedAskOrders[T, A], bidOrders: SortedBidOrders[T, B], pairedOrders: Stream[(A, B)]): Stream[(A, B)] = {
      if (askOrders.isEmpty || bidOrders.isEmpty) {
        pairedOrders
      } else {
        val pair = (askOrders.head, bidOrders.head)
        loop(askOrders.tail, bidOrders.tail, Stream.cons(pair, pairedOrders))
      }
    }
    loop(askOrders, bidOrders, Stream.empty[(A, B)])
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
  def empty[T <: Tradable, A <: LimitAskOrder[T], B <: LimitBidOrder[T]](askOrdering: Ordering[A], bidOrdering: Ordering[B]): MatchedOrders[T, A, B] = {
    new MatchedOrders(TreeSet.empty[A](askOrdering), TreeSet.empty[B](bidOrdering))
  }

}
