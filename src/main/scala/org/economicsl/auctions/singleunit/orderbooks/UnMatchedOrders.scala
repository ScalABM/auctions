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

import org.economicsl.auctions.{Reference, Token}
import org.economicsl.auctions.singleunit.orders._
import org.economicsl.core.{Quantity, Tradable}


/** Class for storing sets of unmatched `AskOrder` and `BidOrder` instances.
  *
  * @param askOrders a heap of `AskOrder` instances.
  * @param bidOrders a heap of `BidOrder` instances.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class UnMatchedOrders[T <: Tradable] private(
  val askOrders: SortedAskOrders[T],
  val bidOrders: SortedBidOrders[T]) {

  require(heapsNotCrossed, "Limit price of best `BidOrder` must not exceed the limit price of the best `AskOrder`.")

  /** The ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance. */
  val askOrdering: Ordering[(Reference, (Token, SingleUnitAskOrder[T]))] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance. */
  val bidOrdering: Ordering[(Reference, (Token, SingleUnitBidOrder[T]))] = bidOrders.ordering

  /** Total number of units of the `Tradable` contained in the `UnMatchedOrders`. */
  val numberUnits: Quantity = askOrders.numberUnits + bidOrders.numberUnits

  /** Create a new `UnMatchedOrders` instance containing the additional `AskOrder`.
    *
    * @param kv
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (Reference, (Token, SingleUnitOrder[T]))): UnMatchedOrders[T] = kv match {
    case (reference, (token, order: SingleUnitAskOrder[T])) =>
      new UnMatchedOrders(askOrders + (reference -> (token -> order)), bidOrders)
    case (reference, (token, order: SingleUnitBidOrder[T])) =>
      new UnMatchedOrders(askOrders, bidOrders + (reference -> (token -> order)))
  }

  /** Remove an order from the collection of unmatched orders.
    *
    * @param reference
    * @return a tuple whose first element is ??? and whose second element is ???
    */
  def - (reference: Reference): (UnMatchedOrders[T], Option[(Token, SingleUnitOrder[T])]) = {
    val (remainingAskOrders, removedAskOrder) = askOrders - reference
    removedAskOrder match {
      case Some(_) =>
        (new UnMatchedOrders(remainingAskOrders, bidOrders), removedAskOrder)
      case None =>
        val (remainingBidOrders, removedBidOrder) = bidOrders - reference
        (new UnMatchedOrders(askOrders, remainingBidOrders), removedBidOrder)
    }
  }

  /** Tests whether some `AskOrder` instance is contained in this `UnMatchedOrders` instance.
    *
    * @param reference the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `UnMatchedOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = askOrders.contains(reference) || bidOrders.contains(reference)

  def get(reference: Reference): Option[(Token, SingleUnitOrder[T])] = {
    askOrders.get(reference).orElse(bidOrders.get(reference))
  }

  def headOption: (Option[(Reference, (Token, SingleUnitAskOrder[T]))], Option[(Reference, (Token, SingleUnitBidOrder[T]))]) = {
    (askOrders.headOption, bidOrders.headOption)
  }

  def isEmpty: Boolean = {
    askOrders.isEmpty && bidOrders.isEmpty
  }

  /**
    *
    * @return
    */
  def tail: UnMatchedOrders[T] = {
    if (askOrders.isEmpty) {
      new UnMatchedOrders(askOrders, bidOrders.tail)
    } else if (bidOrders.isEmpty) {
      new UnMatchedOrders(askOrders.tail, bidOrders)
    } else {
      new UnMatchedOrders(askOrders.tail, bidOrders.tail)  // with throw exception if empty!
    }
  }

  /* Used to check that highest priced bid order does not match with lowest priced ask order. */
  private[this] def heapsNotCrossed: Boolean = {
    bidOrders.headOption.forall{ case (_, (_, bidOrder)) =>
      askOrders.headOption.forall{ case (_, (_, askOrder)) =>
        bidOrder.limit <= askOrder.limit
      }
    }
  }

}


/** Companion object for `UnMatchedOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object UnMatchedOrders {

  /** Create an instance of `UnMatchedOrders`.
    *
    * @param askOrdering ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance.
    * @param bidOrdering ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance.
    * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
    *           type of `Tradable`.
    * @return an instance of `UnMatchedOrders`.
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[SingleUnitAskOrder[T]], bidOrdering: Ordering[SingleUnitBidOrder[T]]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
