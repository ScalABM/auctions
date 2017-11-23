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

import org.economicsl.auctions.messages._
import org.economicsl.core.{Quantity, Tradable}


/** Class for storing sets of matched `AskOrder` and `Bid` instances.
  *
  * @param bids a heap of `Bid` instances that have been matched with `AskOrder` instances in `offers`.
  * @param offers a heap of `AskOrder` instances that have been matched with the `Bid` instances in `bids`
  * @tparam T all `AskOrder` and `Bid` instances stored in this `MatchedOrders` heap should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
private[orderbooks] final class MatchedOrders[T <: Tradable](
  bids: SortedSingleUnitBids[T],
  offers: SortedSingleUnitOffers[T]) {

  type Match = ((OrderReferenceId, (OrderId, SingleUnitOffer[T])), (OrderReferenceId, (OrderId, SingleUnitBid[T])))

  /* If `MatchedOrders` becomes public, then these assert statements should be changed to require statements!*/
  assert(offers.numberUnits == bids.numberUnits)
  assert(invariantsHold, "Limit price of the best `NewSingleUnitBid` must exceed the limit price of the best `NewSingleUnitOffer`.")

  /** The ordering used to sort the `Bid` instances contained in this `MatchedOrders` instance. */
  val bidOrdering: Ordering[(OrderReferenceId, (OrderId, SingleUnitBid[T]))] = bids.ordering

  /** The ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance. */
  val offerOrdering: Ordering[(OrderReferenceId, (OrderId, SingleUnitOffer[T]))] = offers.ordering

  /** Total number of units of the `Tradable` contained in the `MatchedOrders`. */
  val numberUnits: Quantity = offers.numberUnits + bids.numberUnits

  /** Create a new `MatchedOrders` instance containing a matched pair of `(AskOrder, Bid)` instances.
    *
    * @param kv1
    * @param kv2
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `Bid` instances of this
    *         instance and that also contains the matched pair of  `orders`.
    */
  def + (kv1: (OrderReferenceId, (OrderId, SingleUnitOffer[T])), kv2: (OrderReferenceId, (OrderId, SingleUnitBid[T]))): MatchedOrders[T] = {
    new MatchedOrders(bids + kv2, offers + kv1)
  }

  /** Create a new `MatchedOrders` instance with the given matched pair of `(AskOrder, Bid)` removed.
    *
    * @param reference
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `Bid` instances of this
    *         instance but that does not contain the matched pair of  `orders`.
    */
  def - (reference: OrderReferenceId): (MatchedOrders[T], Option[((OrderId, NewSingleUnitOrder[T]), (OrderReferenceId, (OrderId, NewSingleUnitOrder[T])))]) = {
    val (remainingOffers, removedOffer) = offers - reference
    removedOffer match {
      case Some(askOrder) =>
        val (remainingBids, Some(marginalBid)) = bids.splitOffTopOrder
        (new MatchedOrders(remainingBids, remainingOffers), Some((askOrder, marginalBid)))
      case None =>
        val (remainingBids, removedBid) = bids - reference
        removedBid match {
          case Some(bidOrder) =>
            val (remainingOffers, Some(marginalOffer)) = offers.splitOffTopOrder
            (new MatchedOrders(remainingBids, remainingOffers), Some((bidOrder, marginalOffer)))
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
  def contains(reference: OrderReferenceId): Boolean = offers.contains(reference) || bids.contains(reference)

  def get(reference: OrderReferenceId): Option[(OrderId, NewSingleUnitOrder[T])] = {
    offers.get(reference).orElse(bids.get(reference))
  }

  def head: ((OrderReferenceId, (OrderId, SingleUnitOffer[T])), (OrderReferenceId, (OrderId, SingleUnitBid[T]))) = {
    (offers.head, bids.head)
  }

  def headOption: Option[((OrderReferenceId, (OrderId, SingleUnitOffer[T])), (OrderReferenceId, (OrderId, SingleUnitBid[T])))] = {
    offers.headOption.flatMap(askOrder => bids.headOption.map(bidOrder => (askOrder, bidOrder)))
  }

  def isEmpty: Boolean = {
    offers.isEmpty && bids.isEmpty
  }

  /** Replace an existing `NewSingleUnitOffer` instance with another `NewSingleUnitOffer` instance.
    *
    * @param existing
    * @param incoming
    * @return
    */
  def replace(existing: OrderReferenceId, incoming: (OrderReferenceId, (OrderId, NewSingleUnitOrder[T]))): (MatchedOrders[T], (OrderId, NewSingleUnitOrder[T])) = {
    incoming match {
      case (refOrderId, (orderId, order: SingleUnitOffer[T])) =>
        val (remainingOffers, Some(removedOffer)) = offers - existing
        val updatedOffers = remainingOffers + (refOrderId -> (orderId -> order))
        (new MatchedOrders(bids, updatedOffers), removedOffer)
      case (reference, (token, order: SingleUnitBid[T])) =>
        val (remainingBids, Some(removedBid)) = bids - existing
        val updatedBids = remainingBids + (reference -> (token -> order))
        (new MatchedOrders(updatedBids, offers), removedBid)
    }
  }

  /** Split this `MatchedOrders` instance into an optional pair of matched `AskOrder` and `Bid` instances and a
    * residual `MatchedOrders` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, Bid)` instances if this
    *         `MatchedOrders` instance is non empty (first element is `None` otherwise), and whose second element is
    *         the residual `MatchedOrders` instance.
    */
  def splitAtTopMatch: (MatchedOrders[T], Option[Match]) = {
    headOption.fold((this, Option.empty[Match]))(head => (this.tail, Some(head)))
  }

  /** A new `MatchedOrders` instance that contains all matched bids an offers accept the current head.
    *
    * @return
    */
  def tail: MatchedOrders[T] = {
    new MatchedOrders(bids.tail, offers.tail)
  }

  private[this] def invariantsHold: Boolean = {
    bids.headOption.forall{ case (_, (_, bid)) =>
      offers.headOption.forall{ case (_, (_, offer)) =>
        bid.limit >= offer.limit
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
    * @param bidOrdering ordering used to sort the `SingleUnitBid` instances contained in this `MatchedOrders`.
    * @param offerOrdering ordering used to sort the `SingleUnitOffer` instances contained in this `MatchedOrders`.
    * @tparam T all `SingleUnitBid` and `SingleUnitOffer` instances stored in this `MatchedOrders` heap should be for
    *           the same type of `Tradable`.
    * @return an instance of `MatchedOrders`.
    */
  def empty[T <: Tradable](bidOrdering: Ordering[SingleUnitBid[T]], offerOrdering: Ordering[SingleUnitOffer[T]]): MatchedOrders[T] = {
    new MatchedOrders(SortedSingleUnitBids.empty(bidOrdering), SortedSingleUnitOffers.empty(offerOrdering))
  }

}
