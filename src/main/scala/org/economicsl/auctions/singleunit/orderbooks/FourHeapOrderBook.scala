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

import org.economicsl.auctions.OrderBook
import org.economicsl.auctions.messages._
import org.economicsl.core.{Currency, Price, Quantity, Tradable}

import scala.collection.{GenIterable, GenSet}


/** Class implementing the four-heap order book algorithm.
  *
  * @param matchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                      that comprise the current matched set.
  * @param unMatchedOrders contains two heaps of orders, one for `AskOrder` instances and one for `BidOrder` instances,
  *                        that comprise the current unmatched set.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `FourHeapOrderBook` should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  * @note Implementation taken from Wurman et al (1998). Algorithm uses four heaps to organize the submitted orders.
  *       Orders are distinguished by whether or not they are `AskOrder` or `BidOrder` instances, and whether or not
  *       they are in the current matched set.
  */
final class FourHeapOrderBook[T <: Tradable] private(
  val matchedOrders: MatchedOrders[T],
  val unMatchedOrders: UnMatchedOrders[T])
    extends OrderBook[T, NewSingleUnitOrder[T], FourHeapOrderBook[T]] {

  /** If the constructor for `FourHeapOrderBook` becomes public, then this should be changed to require. */
  assert(orderBookInvariantsHold, "FourHeapOrderBook invariants failed!")

  /** Total number of units of the `Tradable` contained in the `FourHeapOrderBook`. */
  val numberUnits: Quantity = matchedOrders.numberUnits + unMatchedOrders.numberUnits

  /** The ask price quote is the price that a buyer would need to exceed in order for its bid to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The ask price quote should be equal to the Mth highest price (where M is the total number of ask orders in
    *       the order book). The ask price quote should be undefined if there are no ask orders in the order book.
    */
  def askPriceQuote: Option[Price] = (matchedOrders.headOption, unMatchedOrders.offers.headOption) match {
    case (Some((_, (_, bid))), Some((_, offer))) =>
      Some(bid.limit min offer.limit)  // offer might have been rationed!
    case (Some((_, (_, bid))), None) =>
      Some(bid.limit)
    case (None, Some((_, offer))) =>
      Some(offer.limit)
    case (None, None) =>
      None
  }

  /** The bid price quote is the price that a seller would need to beat in order for its offer to be matched had the
    * auction cleared at the time the quote was issued.
    *
    * @note The bid price quote should be equal to the (M+1)th highest price (where M is the total number of ask orders
    *       in the order book). The bid price quote should be undefined if there are no bid orders in the order book.
    */
  def bidPriceQuote: Option[Price] = (matchedOrders.headOption, unMatchedOrders.bids.headOption) match {
    case (Some(((_, offer), _)), Some((_, bid))) =>
      Some(bid.limit max offer.limit)  // bid might have been rationed!
    case (Some(((_, offer), _)), None) =>
      Some(offer.limit)
    case (None, Some((_, bid))) =>
      Some(bid.limit)
    case (None, None) =>
      None
  }

  /** The mid-point price quote is an average of the bid and ask price quotes. */
  def midPointPriceQuote: Option[Price] = {
    askPriceQuote.flatMap(askPrice => bidPriceQuote.map(bidPrice => (askPrice / 2) + (bidPrice / 2)))
  }

  def combineWith(that: FourHeapOrderBook[T]): FourHeapOrderBook[T] = {
    // drain that order book of its matched and unmatched orders...
    val (withOutMatchedOrders, additionalMatchedOrders) = that.withoutMatchedOrders
    val (residualOrderBook, additionalUnMatchedOrders) = withOutMatchedOrders.withoutUnMatchedOrders
    assert(residualOrderBook.isEmpty, "After removing all matched and un-matched orders, order book should be empty!")

    // ...and add them to this order book!
    val withAdditionalMatchedOrders = insert(additionalMatchedOrders)
    withAdditionalMatchedOrders.insert(additionalUnMatchedOrders)
  }

  def + (kv: (OrderReferenceId, NewSingleUnitOrder[T])): FourHeapOrderBook[T] = kv match {
    case (orderRefId, order: SingleUnitOffer[T]) =>
      (matchedOrders.headOption, unMatchedOrders.bids.headOption) match {
        case (Some(((_, offer), _)), Some((existing, rationedBidOrder @ bid)))
          if order.limit <= bid.limit && offer.limit <= bid.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (orderRefId -> order, existing -> rationedBidOrder)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
        case (Some(((existing, offer), _)),  _) if order.limit < offer.limit =>
          val (updatedMatchedOrders, replacedAskOrder) = matchedOrders.replace(existing, orderRefId -> order)
          new FourHeapOrderBook(updatedMatchedOrders, unMatchedOrders + (existing -> replacedAskOrder))
        case (None, Some((existing, unMatchedBidOrder @ bid))) if order.limit < bid.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (orderRefId -> order, existing -> unMatchedBidOrder)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
        case _ =>
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (orderRefId -> order))
      }
    case (orderRefId, order: SingleUnitBid[T]) =>
      (matchedOrders.headOption, unMatchedOrders.offers.headOption) match {
        case (Some((_, (_, matchedBidOrder))), Some((existing, rationedAskOrder @ offer)))
          if order.limit >= offer.limit && matchedBidOrder.limit >= offer.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (existing -> rationedAskOrder, orderRefId -> order)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
        case (Some((_, (existing, bid))), _) if order.limit > bid.limit => // no rationing!
          val (updatedMatchedOrders, replacedBidOrder) = matchedOrders.replace(existing, orderRefId -> order)
          new FourHeapOrderBook(updatedMatchedOrders, unMatchedOrders + (existing -> replacedBidOrder))
        case (None, Some((existing, unMatchedAskOrder @ offer))) if order.limit > offer.limit =>
          val (remainingUnMatchedOrders, _) = unMatchedOrders - existing
          val updatedMatchedOrders = matchedOrders + (existing -> unMatchedAskOrder, orderRefId -> order)
          new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
        case _ =>
          new FourHeapOrderBook(matchedOrders, unMatchedOrders + (orderRefId -> order))
      }
  }

  /** Create a new `FourHeapOrderBook` containing the collection of orders.
    *
    * @param kvs
    * @return
    * @note depending on the type of collection `kvs` this method might be done in parallel.
    */
  def insert(kvs: GenIterable[(OrderReferenceId, NewSingleUnitOrder[T])]): FourHeapOrderBook[T] = {
    kvs.aggregate(this)((orderBook, kv) => orderBook + kv, (ob1, ob2) => ob1.combineWith(ob2))
  }

  /**
    *
    * @return `true` if this `FourHeapOrderBook` contains no orders; `false` otherwise.
    */
  def isEmpty: Boolean = {
    matchedOrders.isEmpty && unMatchedOrders.isEmpty
  }

  /**
    *
    * @return `true` if this `FourHeapOrderBook` contains orders; `false` otherwise.
    */
  def nonEmpty: Boolean = {
    !isEmpty
  }

  /** Create a new `FourHeapOrderBook` that does not contain a particular `NewSingleUnitOrder`.
    *
    * @param existing
    * @return
    */
  def - (existing: OrderReferenceId): (FourHeapOrderBook[T], Option[NewSingleUnitOrder[T]]) = {
    val (remainingUnMatchedOrders, removedOrder) = unMatchedOrders - existing
    removedOrder match {
      case Some(_) =>
        (new FourHeapOrderBook(matchedOrders, remainingUnMatchedOrders), removedOrder)
      case None =>
        matchedOrders.get(existing) match {
          case Some(_: SingleUnitOffer[T]) =>
            val (_, (_, bid)) = matchedOrders.head
            unMatchedOrders.offers.headOption match {
              case Some(rationedAskOrder@(orderRefId, offer)) if bid.limit >= offer.limit =>
                val (remainingUnMatchedOrders, _) = unMatchedOrders - orderRefId
                val (updatedMatchedOrders, removedAskOrder) = matchedOrders.replace(existing, rationedAskOrder)
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, Some(removedAskOrder))
              case _ =>
                val (remainingMatchedOrders, Some((removedAskOrder, marginalBidOrder))) = matchedOrders - existing
                val updatedUnMatchedOrders = unMatchedOrders + marginalBidOrder
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, Some(removedAskOrder))
            }
          case Some(_: SingleUnitBid[T]) =>
            val ((_, offer), _) = matchedOrders.head
            unMatchedOrders.bids.headOption match {
              case Some(rationedBidOrder@(orderRefId, bid)) if bid.limit >= offer.limit =>
                val (updatedMatchedOrders, removedBidOrder) = matchedOrders.replace(existing, rationedBidOrder)
                val (remainingUnMatchedOrders, _) = unMatchedOrders - orderRefId
                val updatedOrderBook = new FourHeapOrderBook(updatedMatchedOrders, remainingUnMatchedOrders)
                (updatedOrderBook, Some(removedBidOrder))
              case _ =>
                val (remainingMatchedOrders, Some((removedBidOrder, marginalAskOrder))) = matchedOrders - existing
                val updatedUnMatchedOrders = unMatchedOrders + marginalAskOrder
                val updatedOrderBook = new FourHeapOrderBook(remainingMatchedOrders, updatedUnMatchedOrders)
                (updatedOrderBook, Some(removedBidOrder))
            }
          case None =>
            (this, None)


        }
    }
  }

  /** Split this `FourHeapOrderBook` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `FourHeapOrderBook` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if the underlying
    *         `MatchedOrders` instance is non-empty (first element is `None` otherwise), and whose second element is
    *         the residual `FourHeapOrderBook` instance.
    */
  def splitAtTopMatch: (FourHeapOrderBook[T], Option[((OrderReferenceId, SingleUnitOffer[T]), (OrderReferenceId, SingleUnitBid[T]))]) = {
    val (remainingMatchedOrders, topMatchedOrders) = matchedOrders.splitAtTopMatch
    (new FourHeapOrderBook(remainingMatchedOrders, unMatchedOrders), topMatchedOrders)
  }

  def spread: Option[Currency] = {
    bidPriceQuote.flatMap(bidPrice => askPriceQuote.map(askPrice => bidPrice.value - askPrice.value))
  }

  /**
    *
    * @return
    * @todo benchmark this method!
    */
  private def withoutMatchedOrders: (FourHeapOrderBook[T], GenSet[(OrderReferenceId, NewSingleUnitOrder[T])]) = {

    @annotation.tailrec
    def accumulate(orderBook: FourHeapOrderBook[T], orders: GenSet[(OrderReferenceId, NewSingleUnitOrder[T])]): (FourHeapOrderBook[T], GenSet[(OrderReferenceId, NewSingleUnitOrder[T])]) = {
      val (residualOrderBook, topMatch) = orderBook.splitAtTopMatch
      topMatch match {
        case Some((offer, bid)) =>
          val updatedOrders = orders + offer + bid
          accumulate(residualOrderBook, updatedOrders)
        case None =>
          (residualOrderBook, orders)
      }
    }

    accumulate(this, GenSet.empty)
  }

  /**
    *
    * @return
    * @todo benchmark this method!
    */
  private def withoutUnMatchedOrders: (FourHeapOrderBook[T], GenIterable[(OrderReferenceId, NewSingleUnitOrder[T])]) = {

    @annotation.tailrec
    def accumulate(orderBook: FourHeapOrderBook[T], orders: GenSet[(OrderReferenceId, NewSingleUnitOrder[T])]): (FourHeapOrderBook[T], GenSet[(OrderReferenceId, NewSingleUnitOrder[T])]) = {
      orderBook.unMatchedOrders.headOption match {
        case (Some(offer), Some(bid)) =>
          val updatedOrders = orders + offer + bid
          val residualUnMatchedOrders = orderBook.unMatchedOrders.tail
          val residualOrderBook = new FourHeapOrderBook(matchedOrders, residualUnMatchedOrders)
          accumulate(residualOrderBook, updatedOrders)
        case (Some(offer), None) =>
          val updatedOrders = orders + offer
          val residualUnMatchedOrders = orderBook.unMatchedOrders.tail
          val residualOrderBook = new FourHeapOrderBook(matchedOrders, residualUnMatchedOrders)
          accumulate(residualOrderBook, updatedOrders)
        case (None, Some(bid)) =>
          val updatedOrders = orders + bid
          val residualUnMatchedOrders = orderBook.unMatchedOrders.tail
          val residualOrderBook = new FourHeapOrderBook(matchedOrders, residualUnMatchedOrders)
          accumulate(residualOrderBook, updatedOrders)
        case (None, None) =>
          (orderBook, orders)
      }
    }

    accumulate(this, GenSet.empty)
  }

  /**
    *
    * @return true if order book invariants hold; false otherwise.
    */
  private[this] def orderBookInvariantsHold: Boolean = {
    matchedOrders.headOption.forall { case ((_, a1), (_, b1)) =>
      unMatchedOrders.offers.headOption.forall { case (_, a2) =>
        unMatchedOrders.bids.headOption.forall { case (_, b2) =>
          b1.limit >= b2.limit && a2.limit >= a1.limit
        }
      }
    }
  }

}


/** Companion object for `FourHeapOrderBook`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object FourHeapOrderBook {

  def empty[T <: Tradable](biding: Ordering[SingleUnitBid[T]], offerOrdering: Ordering[SingleUnitOffer[T]]): FourHeapOrderBook[T] = {
    val matchedOrders = MatchedOrders.empty(biding, offerOrdering.reverse)
    val unMatchedOrders = UnMatchedOrders.empty(biding.reverse, offerOrdering)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

