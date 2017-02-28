package org.economicsl.auctions.orderbooks

import org.economicsl.auctions.{LimitAskOrder, LimitBidOrder, SingleUnit}

import scala.collection.immutable.TreeSet


class UnMatchedOrders private(val askOrders: SortedAskOrders, val bidOrders: SortedBidOrders) {

  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit <= askOrders.head.limit))

  def + (order: LimitAskOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders + order, bidOrders)

  def + (order: LimitBidOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders, bidOrders + order)

  def - (order: LimitAskOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders - order, bidOrders)

  def - (order: LimitBidOrder with SingleUnit): UnMatchedOrders = new UnMatchedOrders(askOrders, bidOrders - order)

  def contains(order: LimitAskOrder with SingleUnit): Boolean = askOrders.contains(order)

  def contains(order: LimitBidOrder with SingleUnit): Boolean = bidOrders.contains(order)

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
  def empty(askOrdering: Ordering[LimitAskOrder with SingleUnit], bidOrdering: Ordering[LimitBidOrder with SingleUnit]): UnMatchedOrders = {
    new UnMatchedOrders(TreeSet.empty[LimitAskOrder with SingleUnit](askOrdering), TreeSet.empty[LimitBidOrder with SingleUnit](bidOrdering))
  }

}
