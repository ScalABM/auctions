package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy}


final class SealedBidAuction[T <: Tradable] private(_orderBook: FourHeapOrderBook[T], _pricingPolicy: PricingPolicy[T])
  extends Auction[T, SealedBidAuction[T]] {

  def insert(order: LimitBidOrder[T]): SealedBidAuction[T] = {
    new SealedBidAuction(orderBook.insert(order), pricingPolicy)
  }

  def remove(order: LimitBidOrder[T]): SealedBidAuction[T] = {
    new SealedBidAuction(orderBook.remove(order), pricingPolicy)
  }

  def clear: ClearResult[T, SealedBidAuction[T]] = {
    pricingPolicy(orderBook) match {
      case Some(price) =>
        val (matchedOrders, residualOrderBook) = orderBook.takeAllMatched
        val fills = matchedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        ClearResult(Some(fills), new SealedBidAuction(residualOrderBook, pricingPolicy))
      case None => ClearResult[T, SealedBidAuction[T]](None, this)
    }
  }

  protected val orderBook: FourHeapOrderBook[T] = _orderBook

  protected val pricingPolicy: PricingPolicy[T] = _pricingPolicy

}


object SealedBidAuction {

  def apply[T <: Tradable](reservation: LimitAskOrder[T], pricingPolicy: PricingPolicy[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction(orderBook.insert(reservation), pricingPolicy)
  }

  /** Create a first-price, sealed-bid `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def firstPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy)
  }

  /** Create a second-price, sealed-bid or Vickrey `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def secondPriceSealedBid[T <: Tradable](reservation: LimitAskOrder[T]): SealedBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new SealedBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy)
  }

  /** Used to create an auction with a sealed order book without specifying the pricing policy.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withClosedOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithClosedOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithClosedOrderBook[T](orderBook)
  }


  final class WithClosedOrderBook[T <: Tradable] (orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitBidOrder[T]): WithClosedOrderBook[T] = {
      new WithClosedOrderBook[T](orderBook.remove(order))
    }

    def withPricingPolicy(pricingPolicy: PricingPolicy[T]): SealedBidAuction[T] = {
      new SealedBidAuction[T](orderBook, pricingPolicy)
    }

  }

}
