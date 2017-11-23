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


/** Class for storing sets of unmatched `AskOrder` and `BidOrder` instances.
  *
  * @param offers a heap of `AskOrder` instances.
  * @param bids a heap of `BidOrder` instances.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
private[orderbooks] final class UnMatchedOrders[T <: Tradable] private(
  protected [orderbooks] val bids: SortedSingleUnitBids[T],
  protected[orderbooks] val offers: SortedSingleUnitOffers[T]) {

  /* If `UnMatchedOrders` becomes public, then assert statement should be changed to require statement!*/
  assert(heapsNotCrossed, "Limit price of best `SingleUnitBid` must not exceed the limit price of the best `SingleUnitOffer`.")

  /** The ordering used to sort the `BidOrder` instances contained in this `UnMatchedOrders` instance. */
  val bidOrdering: Ordering[(OrderReferenceId, (OrderId, NewSingleUnitBid[T]))] = bids.ordering

  /** The ordering used to sort the `AskOrder` instances contained in this `UnMatchedOrders` instance. */
  val offerOrdering: Ordering[(OrderReferenceId, (OrderId, NewSingleUnitOffer[T]))] = offers.ordering

  /** Total number of units of the `Tradable` contained in the `UnMatchedOrders`. */
  val numberUnits: Quantity = offers.numberUnits + bids.numberUnits

  /** Create a new `UnMatchedOrders` instance containing the additional `AskOrder`.
    *
    * @param kv
    * @return a new `UnMatchedOrders` instance that contains all of the `AskOrder` instances of this instance and that
    *         also contains the `order`.
    */
  def + (kv: (OrderReferenceId, (OrderId, NewSingleUnitOrder[T]))): UnMatchedOrders[T] = kv match {
    case (orderRefId, (orderId, order: NewSingleUnitOffer[T])) =>
      new UnMatchedOrders(bids, offers + (orderRefId -> (orderId -> order)))
    case (orderRefId, (orderId, order: NewSingleUnitBid[T])) =>
      new UnMatchedOrders(bids + (orderRefId -> (orderId -> order)), offers)
  }

  /** Remove an order from the collection of unmatched orders.
    *
    * @param orderRefId
    * @return a tuple whose first element is ??? and whose second element is ???
    */
  def - (orderRefId: OrderReferenceId): (UnMatchedOrders[T], Option[(OrderId, NewSingleUnitOrder[T])]) = {
    val (remainingAskOrders, removedAskOrder) = offers - orderRefId
    removedAskOrder match {
      case Some(_) =>
        (new UnMatchedOrders(bids, remainingAskOrders), removedAskOrder)
      case None =>
        val (remainingBidOrders, removedBidOrder) = bids - orderRefId
        (new UnMatchedOrders(remainingBidOrders, offers), removedBidOrder)
    }
  }

  /** Tests whether some `AskOrder` instance is contained in this `UnMatchedOrders` instance.
    *
    * @param reference the `AskOrder` instance to test for membership.
    * @return `true` if the `order` is contained in this `UnMatchedOrders` instance; `false` otherwise.
    */
  def contains(reference: OrderReferenceId): Boolean = offers.contains(reference) || bids.contains(reference)

  def get(reference: OrderReferenceId): Option[(OrderId, NewSingleUnitOrder[T])] = {
    offers.get(reference).orElse(bids.get(reference))
  }

  def headOption: (Option[(OrderReferenceId, (OrderId, NewSingleUnitOffer[T]))], Option[(OrderReferenceId, (OrderId, NewSingleUnitBid[T]))]) = {
    (offers.headOption, bids.headOption)
  }

  def isEmpty: Boolean = {
    offers.isEmpty && bids.isEmpty
  }

  /**
    *
    * @return
    */
  def tail: UnMatchedOrders[T] = {
    if (offers.isEmpty) {
      new UnMatchedOrders(bids.tail, offers)
    } else if (bids.isEmpty) {
      new UnMatchedOrders(bids, offers.tail)
    } else {
      new UnMatchedOrders(bids.tail, offers.tail)  // with throw exception if empty!
    }
  }

  /* Used to check that highest priced bid order does not match with lowest priced ask order. */
  private[this] def heapsNotCrossed: Boolean = {
    bids.headOption.forall{ case (_, (_, bidOrder)) =>
      offers.headOption.forall{ case (_, (_, askOrder)) =>
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
    * @param bidOrdering ordering used to sort the `SingleUnitBid` instances contained in this `UnMatchedOrders`.
    * @param offerOrdering ordering used to sort the `SingleUnitOffer` instances contained in this `UnMatchedOrders`.
    * @tparam T all `AskOrder` and `BidOrder` instances stored in this `UnMatchedOrders` heap should be for the same
    *           type of `Tradable`.
    * @return an instance of `UnMatchedOrders`.
    * @note the heap used to store store the `AskOrder` instances is ordered from low to high
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from high to low based on `limit` price.
    */
  def empty[T <: Tradable](bidOrdering: Ordering[NewSingleUnitBid[T]], offerOrdering: Ordering[NewSingleUnitOffer[T]]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedSingleUnitBids.empty(bidOrdering), SortedSingleUnitOffers.empty(offerOrdering))
  }

}
