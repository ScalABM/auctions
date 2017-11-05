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

import org.economicsl.auctions.messages.{OrderId, OrderReferenceId}
import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder, SingleUnitBidOrder, SingleUnitOrder}
import org.economicsl.core.{Quantity, Tradable}


/** Class for storing sets of matched `AskOrder` and `BidOrder` instances.
  *
  * @param askOrders a heap of `AskOrder` instances that have been matched with the `BidOrder` instances in `bidOrders`
  * @param bidOrders a heap of `BidOrder` instances that have been matched with `AskOrder` instances in `askOrders`.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `MatchedOrders` heap should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
private[orderbooks] final class MatchedOrders[T <: Tradable](askOrders: SortedAskOrders[T], bidOrders: SortedBidOrders[T]) {

  type Match = ((OrderReferenceId, (OrderId, SingleUnitAskOrder[T])), (OrderReferenceId, (OrderId, SingleUnitBidOrder[T])))

  /* If `MatchedOrders` becomes public, then these should be changes to require!*/
  assert(askOrders.numberUnits == bidOrders.numberUnits)
  assert(invariantsHold, "Limit price of the best `BidOrder` must exceed the limit price of the best `AskOrder`.")

  /** The ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance. */
  val askOrdering: Ordering[(OrderReferenceId, (OrderId, SingleUnitAskOrder[T]))] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `MatchedOrders` instance. */
  val bidOrdering: Ordering[(OrderReferenceId, (OrderId, SingleUnitBidOrder[T]))] = bidOrders.ordering

  /** Total number of units of the `Tradable` contained in the `MatchedOrders`. */
  val numberUnits: Quantity = askOrders.numberUnits + bidOrders.numberUnits

  /** Create a new `MatchedOrders` instance containing a matched pair of `(AskOrder, BidOrder)` instances.
    *
    * @param kv1
    * @param kv2
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance and that also contains the matched pair of  `orders`.
    */
  def + (kv1: (OrderReferenceId, (OrderId, SingleUnitAskOrder[T])), kv2: (OrderReferenceId, (OrderId, SingleUnitBidOrder[T]))): MatchedOrders[T] = {
    new MatchedOrders(askOrders + kv1, bidOrders + kv2)
  }

  /** Create a new `MatchedOrders` instance with the given matched pair of `(AskOrder, BidOrder)` removed.
    *
    * @param reference
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance but that does not contain the matched pair of  `orders`.
    */
  def - (reference: OrderReferenceId): (MatchedOrders[T], Option[((OrderId, SingleUnitOrder[T]), (OrderReferenceId, (OrderId, SingleUnitOrder[T])))]) = {
    val (remainingAskOrders, removedAskOrder) = askOrders - reference
    removedAskOrder match {
      case Some(askOrder) =>
        val (remainingBidOrders, Some(marginalBidOrder)) = bidOrders.splitOffTopOrder
        (new MatchedOrders(remainingAskOrders, remainingBidOrders), Some((askOrder, marginalBidOrder)))
      case None =>
        val (remainingBidOrders, removedBidOrder) = bidOrders - reference
        removedBidOrder match {
          case Some(bidOrder) =>
            val (remainingAskOrders, Some(marginalAskOrder)) = askOrders.splitOffTopOrder
            (new MatchedOrders(remainingAskOrders, remainingBidOrders), Some((bidOrder, marginalAskOrder)))
          case None =>
            (this, None)
        }
    }
  }

  /** Tests whether this `MatchedOrders` instance contains an `Order` associated with a given reference identifier.
    *
    * @param reference the `Reference` instance to test for membership.
    * @return `true` if the `order` is contained in this `MatchedOrders` instance; `false` otherwise.
    */
  def contains(reference: OrderReferenceId): Boolean = askOrders.contains(reference) || bidOrders.contains(reference)

  def get(reference: OrderReferenceId): Option[(OrderId, SingleUnitOrder[T])] = {
    askOrders.get(reference).orElse(bidOrders.get(reference))
  }

  def head: ((OrderReferenceId, (OrderId, SingleUnitAskOrder[T])), (OrderReferenceId, (OrderId, SingleUnitBidOrder[T]))) = {
    (askOrders.head, bidOrders.head)
  }

  def headOption: Option[((OrderReferenceId, (OrderId, SingleUnitAskOrder[T])), (OrderReferenceId, (OrderId, SingleUnitBidOrder[T])))] = {
    askOrders.headOption.flatMap(askOrder => bidOrders.headOption.map(bidOrder => (askOrder, bidOrder)))
  }

  def isEmpty: Boolean = {
    askOrders.isEmpty && bidOrders.isEmpty
  }

  /** Replace an existing `AskOrder` instance with another `AskOrder` instance.
    *
    * @param existing
    * @param incoming
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` except the `reference` `AskOrder`
    *         instance and that also contains the `kv` mapping `(Reference, (Token, AskOrder))`.
    */
  def replace(existing: OrderReferenceId, incoming: (OrderReferenceId, (OrderId, SingleUnitOrder[T]))): (MatchedOrders[T], (OrderId, SingleUnitOrder[T])) = {
    incoming match {
      case (refOrderId, (orderId, order: SingleUnitAskOrder[T])) =>
        val (remainingAskOrders, Some(removedAskOrder)) = askOrders - existing
        val updatedAskOrders = remainingAskOrders + (refOrderId -> (orderId -> order))
        (new MatchedOrders(updatedAskOrders, bidOrders), removedAskOrder)
      case (reference, (token, order: SingleUnitBidOrder[T])) =>
        val (remainingBidOrders, Some(removedBidOrder)) = bidOrders - existing
        val updatedBidOrders = remainingBidOrders + (reference -> (token -> order))
        (new MatchedOrders(askOrders, updatedBidOrders), removedBidOrder)
    }
  }

  /** Split this `MatchedOrders` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `MatchedOrders` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if this
    *         `MatchedOrders` instance is non empty (first element is `None` otherwise), and whose second element is
    *         the residual `MatchedOrders` instance.
    */
  def splitAtTopMatch: (MatchedOrders[T], Option[Match]) = {
    headOption.fold((this, Option.empty[Match]))(head => (this.tail, Some(head)))
  }

  def tail: MatchedOrders[T] = {
    new MatchedOrders(askOrders.tail, bidOrders.tail)
  }

  private[this] def invariantsHold: Boolean = {
    bidOrders.headOption.forall{ case (_, (_, bidOrder)) =>
      askOrders.headOption.forall{ case (_, (_, askOrder)) =>
        bidOrder.limit >= askOrder.limit
      }
    }
  }

}


/** Companion object for `MatchedOrders`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @param askOrdering ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance.
    * @param bidOrdering ordering used to sort the `BidOrder` instances contained in this `MatchedOrders` instance.
    * @tparam T all `AskOrder` and `BidOrder` instances stored in this `MatchedOrders` heap should be for the same
    *           type of `Tradable`.
    * @return an instance of `MatchedOrders`.
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty[T <: Tradable](askOrdering: Ordering[SingleUnitAskOrder[T]], bidOrdering: Ordering[SingleUnitBidOrder[T]]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
