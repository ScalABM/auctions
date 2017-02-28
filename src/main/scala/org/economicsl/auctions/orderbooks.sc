import org.economicsl.auctions.{LimitAskOrder, LimitBidOrder, SingleUnit}

import scala.collection.immutable.TreeSet

type SortedAskOrders = TreeSet[LimitAskOrder with SingleUnit]
type SortedBidOrders = TreeSet[LimitBidOrder with SingleUnit]


class MatchedOrders private(val askOrders: SortedAskOrders, val bidOrders: SortedBidOrders) {

  require(askOrders.size == bidOrders.size)  // number of units must be the same!
  require(bidOrders.headOption.forall(bidOrder => bidOrder.limit >= askOrders.head.limit))  // value of lowest bid must exceed value of highest ask!

  def + (orders: (LimitAskOrder with SingleUnit, LimitBidOrder with SingleUnit)): MatchedOrders = {
    new MatchedOrders(askOrders + orders._1, bidOrders + orders._2)
  }

  def - (orders: (LimitAskOrder with SingleUnit, LimitBidOrder with SingleUnit)): MatchedOrders = {
    new MatchedOrders(askOrders - orders._1, bidOrders - orders._2)
  }

  def contains(order: LimitAskOrder with SingleUnit): Boolean = askOrders.contains(order)

  def contains(order: LimitBidOrder with SingleUnit): Boolean = bidOrders.contains(order)

  def replace(existing: LimitAskOrder with SingleUnit, incoming: LimitAskOrder with SingleUnit): MatchedOrders = {
    new MatchedOrders(askOrders - existing + incoming, bidOrders)
  }

  def replace(existing: LimitBidOrder with SingleUnit, incoming: LimitBidOrder with SingleUnit): MatchedOrders = {
    new MatchedOrders(askOrders, bidOrders - existing + incoming)
  }

}


object MatchedOrders {

  /** Create an instance of `MatchedOrders`.
    *
    * @param askOrdering
    * @param bidOrdering
    * @return
    * @note the heap used to store store the `AskOrder` instances is ordered from high to low
    *       based on `limit` price; the heap used to store store the `BidOrder` instances is
    *       ordered from low to high based on `limit` price.
    */
  def empty(askOrdering: Ordering[LimitAskOrder with SingleUnit], bidOrdering: Ordering[LimitBidOrder with SingleUnit]): MatchedOrders = {

    new MatchedOrders(TreeSet.empty[AskOrder](askOrdering), TreeSet.empty[BidOrder](bidOrdering))

  }

}