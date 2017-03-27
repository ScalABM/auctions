package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingRule


class Auction[T <: Tradable] private(orderBook: FourHeapOrderBook[T]) extends AuctionLike[T, Auction[T]] {

  def insert(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook + order)
  }

  def remove(order: LimitBidOrder[T]): Auction[T] = {
    new Auction(orderBook - order)
  }

  def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], Auction[T]) = {
    p(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new Auction(newOrderBook))
      case None => (None, new Auction(orderBook))
    }
  }

}


object Auction{

  def withEmptyOrderBook[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): Auction[T] = {
    new Auction[T](FourHeapOrderBook.empty[T](askOrdering, bidOrdering))
  }

}