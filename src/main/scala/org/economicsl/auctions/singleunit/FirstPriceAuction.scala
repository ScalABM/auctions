package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingRule


class FirstPriceAuction[T <: Tradable] private(orderBook: FourHeapOrderBook[T]) extends AuctionLike[T, FirstPriceAuction[T]] {

  def insert(order: LimitBidOrder[T]): FirstPriceAuction[T] = {
    new FirstPriceAuction(orderBook + order)
  }

  def remove(order: LimitBidOrder[T]): FirstPriceAuction[T] = {
    new FirstPriceAuction(orderBook - order)
  }

  def clear: (Option[Stream[Fill[T]]], FirstPriceAuction[T]) = {
    p(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        (Some(fills), new FirstPriceAuction(newOrderBook))
      case None => (None, new FirstPriceAuction(orderBook))
    }
  }

  private[this] val p: AskQuotePricingRule[T] = new AskQuotePricingRule[T]

}


object FirstPriceAuction{

  def withEmptyOrderBook[T <: Tradable](implicit askOrdering: Ordering[LimitAskOrder[T]], bidOrdering: Ordering[LimitBidOrder[T]]): FirstPriceAuction[T] = {
    new FirstPriceAuction[T](FourHeapOrderBook.empty[T](askOrdering, bidOrdering))
  }

}