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

import java.util.UUID

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.multiunit.{LimitAskOrder, LimitBidOrder}


private[orderbooks] case class UnMatchedOrders[T <: Tradable](askOrders: SortedAskOrders[T],
                                                              bidOrders: SortedBidOrders[T]) {

  assert(bidOrders.headOption.forall{ case (_, bidOrder) => askOrders.headOption.forall{ case (_, askOrder) => bidOrder.limit <= askOrder.limit } })

  val isEmpty: Boolean = askOrders.isEmpty && bidOrders.isEmpty

  val nonEmpty: Boolean = askOrders.nonEmpty || bidOrders.nonEmpty

  /** Add a new `LimitAskOrder` to the collection of unmatched orders .*/
  def + (uuid: UUID, order: LimitAskOrder[T]): UnMatchedOrders[T] = {
    UnMatchedOrders(askOrders + (uuid -> order), bidOrders)
  }

  /** Add a new `LimitBidOrder` to the collection of unmatched orders .*/
  def + (uuid: UUID, order: LimitBidOrder[T]): UnMatchedOrders[T] = {
    UnMatchedOrders(askOrders, bidOrders + (uuid -> order))
  }

  /** Remove an order from the collection of unmatched orders. */
  def - (uuid: UUID): UnMatchedOrders[T] = {
    if (askOrders.contains(uuid)) {
      UnMatchedOrders(askOrders - uuid, bidOrders)
    } else {
      UnMatchedOrders(askOrders, bidOrders - uuid)
    }
  }

  val askOrdering: Ordering[(UUID, LimitAskOrder[T])] = askOrders.ordering

  val bidOrdering: Ordering[(UUID, LimitBidOrder[T])] = bidOrders.ordering

  /** Check whether an order is contained in the collection of unmatched orders using. */
  def contains(uuid: UUID): Boolean = askOrders.contains(uuid) || bidOrders.contains(uuid)

  def mergeWith(other: UnMatchedOrders[T]): UnMatchedOrders[T] = {
    UnMatchedOrders(askOrders.mergeWith(other.askOrders), bidOrders.mergeWith(bidOrders))
  }

  /** Add a `LimitAskOrder` to the collection of unmatched orders. */
  def updated(uuid: UUID, order: LimitAskOrder[T]): UnMatchedOrders[T] = {
    UnMatchedOrders(askOrders.update(uuid, order), bidOrders)
  }

  /** Add a `LimitBidOrder` to the collection of unmatched orders. */
  def updated(uuid: UUID, order: LimitBidOrder[T]): UnMatchedOrders[T] = {
    UnMatchedOrders(askOrders, bidOrders.updated(uuid, order))
  }

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
  def empty[T <: Tradable](askOrdering: Ordering[(UUID, LimitAskOrder[T])], bidOrdering: Ordering[(UUID, LimitBidOrder[T])]): UnMatchedOrders[T] = {
    new UnMatchedOrders(SortedAskOrders.empty(askOrdering), SortedBidOrders.empty(bidOrdering))
  }

}
