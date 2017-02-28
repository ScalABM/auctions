package org.economicsl.auctions.orderbooks

import org.economicsl.auctions._


class FourHeapOrderBook private(val matchedOrders: MatchedOrders, val unMatchedOrders: UnMatchedOrders) {

  def - (order: LimitAskOrder with SingleUnit): FourHeapOrderBook = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else {
      val bidOrder = matchedOrders.bidOrders.head
      new FourHeapOrderBook(matchedOrders - (order, bidOrder), unMatchedOrders + bidOrder)
    }
  }

  def - (order: LimitBidOrder with SingleUnit): FourHeapOrderBook = {
    if (unMatchedOrders.contains(order)) {
      new FourHeapOrderBook(matchedOrders, unMatchedOrders - order)
    } else {
      val askOrder = matchedOrders.askOrders.head
      new FourHeapOrderBook(matchedOrders - (askOrder, order), unMatchedOrders + askOrder)
    }
  }

  def + (order: LimitAskOrder with SingleUnit): FourHeapOrderBook = {
    (matchedOrders.askOrders.headOption, unMatchedOrders.bidOrders.headOption) match {
      case (Some(askOrder), Some(bidOrder)) if order.limit <= bidOrder.limit && askOrder.limit <= bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (order, bidOrder), unMatchedOrders - bidOrder)
      case (None, Some(bidOrder)) if order.limit <= bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (order, bidOrder), unMatchedOrders - bidOrder)
      case (Some(askOrder), Some(_)) if order.limit < askOrder.limit =>
        new FourHeapOrderBook(matchedOrders.replace(askOrder, order), unMatchedOrders + askOrder)
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  def + (order: LimitBidOrder with SingleUnit): FourHeapOrderBook = {
    (matchedOrders.bidOrders.headOption, unMatchedOrders.askOrders.headOption) match {
      case (Some(bidOrder), Some(askOrder)) if order.limit >= askOrder.limit && bidOrder.limit >= askOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (askOrder, order), unMatchedOrders - askOrder)
      case (None, Some(askOrder)) if order.limit >= askOrder.limit =>
        new FourHeapOrderBook(matchedOrders + (askOrder, order), unMatchedOrders - askOrder)
      case (Some(bidOrder), Some(_)) if order.limit > bidOrder.limit =>
        new FourHeapOrderBook(matchedOrders.replace(bidOrder, order), unMatchedOrders + bidOrder)
      case _ =>
        new FourHeapOrderBook(matchedOrders, unMatchedOrders + order)
    }
  }

  def dropMatchedOrders: FourHeapOrderBook = {
    val (askOrdering, bidOrdering) = (matchedOrders.askOrders.ordering, matchedOrders.bidOrders.ordering)
    new FourHeapOrderBook(MatchedOrders.empty(askOrdering, bidOrdering), unMatchedOrders)
  }

}


object FourHeapOrderBook {

  def empty(implicit askOrdering: Ordering[LimitAskOrder with SingleUnit], bidOrdering: Ordering[LimitBidOrder with SingleUnit]): FourHeapOrderBook = {
    val matchedOrders = MatchedOrders.empty(askOrdering.reverse, bidOrdering.reverse)
    val unMatchedOrders = UnMatchedOrders.empty(askOrdering, bidOrdering)
    new FourHeapOrderBook(matchedOrders, unMatchedOrders)
  }

}

