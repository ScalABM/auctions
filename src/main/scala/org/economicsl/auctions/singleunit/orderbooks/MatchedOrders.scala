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
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.core.Tradable

/** Class for storing sets of matched `AskOrder` and `BidOrder` instances.
  *
  * @param askOrders a heap of `AskOrder` instances that have been matched with the `BidOrder` instances in `bidOrders`
  * @param bidOrders a heap of `BidOrder` instances that have been matched with `AskOrder` instances in `askOrders`.
  * @tparam T all `AskOrder` and `BidOrder` instances stored in this `MatchedOrders` heap should be for the same type
  *           of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
final class MatchedOrders[T <: Tradable] private(val askOrders: SortedAskOrders[T], val bidOrders: SortedBidOrders[T]) {

  /* Number of units supplied must equal the number of units demanded. */
  require(askOrders.numberUnits == bidOrders.numberUnits)

  require(crossed, "Limit price of the best `BidOrder` must exceed the limit price of the best `AskOrder`.")

  /** The ordering used to sort the `AskOrder` instances contained in this `MatchedOrders` instance. */
  val askOrdering: Ordering[AskOrder[T]] = askOrders.ordering

  /** The ordering used to sort the `BidOrder` instances contained in this `MatchedOrders` instance. */
  val bidOrdering: Ordering[(Reference, BidOrder[T])] = bidOrders.ordering

  /** Create a new `MatchedOrders` instance containing a matched pair of `(AskOrder, BidOrder)` instances.
    *
    * @param kvs a matched pair of mappings `(Reference, AskOrder)`, `(Reference, BidOrder)` that should be added.
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance and that also contains the matched pair of  `orders`.
    */
  def + (kvs: ((Reference, AskOrder[T]), (Reference, BidOrder[T]))): MatchedOrders[T] = {
    new MatchedOrders(askOrders + kvs._1, bidOrders + kvs._2)
  }

  /** Create a new `MatchedOrders` instance with the given matched pair of `(AskOrder, BidOrder)` removed.
    *
    * @param references
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` and `BidOrder` instances of this
    *         instance but that does not contain the matched pair of  `orders`.
    */
  def - (references: (Reference, Reference)): MatchedOrders[T] = {
    new MatchedOrders(askOrders - references._1, bidOrders - references._2)
  }

  /** Tests whether this `MatchedOrders` instance contains an `Order` associated with a given reference identifier.
    *
    * @param reference the `Reference` instance to test for membership.
    * @return `true` if the `order` is contained in this `MatchedOrders` instance; `false` otherwise.
    */
  def contains(reference: Reference): Boolean = askOrders.contains(reference) || bidOrders.contains(reference)

  /** Replace an existing `AskOrder` instance with another `AskOrder` instance.
    *
    * @param reference
    * @param kv
    * @return a new `MatchedOrders` instance that contains all of the `AskOrder` except the `reference` `AskOrder`
    *         instance and that also contains the `kv` mapping `(Reference, (Token, AskOrder))`.
    */
  def replace(reference: Reference, kv: (Reference, (Token, Order[T]))): MatchedOrders[T] =  kv match {
    case incoming @ (_,  (_, _: AskOrder[T])) if askOrders.contains(reference) =>
      new MatchedOrders(askOrders - reference + incoming, bidOrders)
    case incoming @ (_,  (_, _: BidOrder[T])) if bidOrders.contains(reference) =>
      new MatchedOrders(askOrders, bidOrders - reference + incoming)
    case _ =>
      this  // should (probably!) never occur in practice...
  }

  /** Split this `MatchedOrders` instance into an optional pair of matched `AskOrder` and `BidOrder` instances and a
    * residual `MatchedOrders` instance.
    *
    * @return a `Tuple` whose first element is some matched pair of `(AskOrder, BidOrder)` instances if this
    *         `MatchedOrders` instance is non empty (first element is `None` otherwise), and whose second element is
    *         the residual `MatchedOrders` instance.
    */
  def splitAtBestMatch: (Option[((Reference, AskOrder[T]), (Reference, BidOrder[T]))], MatchedOrders[T]) = {
    (askOrders.headOption, bidOrders.headOption) match {
      case (Some((reference, askOrder)), Some(bidOrder)) =>
        (Some((askOrder, bidOrder)), new MatchedOrders(askOrders - askOrder, bidOrders - bidOrder))
      case _ => (None, this)
    }
  }

  private[this] def crossed: Boolean = {
    bidOrders.headOption.forall{ case (_, bidOrder) =>
      askOrders.headOption.forall{ askOrder =>
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
  def empty[T <: Tradable](askOrdering: Ordering[AskOrder[T]], bidOrdering: Ordering[(Reference, BidOrder[T])]): MatchedOrders[T] = {
    new MatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
