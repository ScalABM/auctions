package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy, PricingPolicy}
import org.economicsl.auctions.singleunit.quoting.{PriceQuotePolicy, PriceQuoting}


class OpenBidAuction[T <: Tradable] private(val orderBook: FourHeapOrderBook[T],
                                            val pricingPolicy: PricingPolicy[T],
                                            val quotingPolicy: PriceQuotePolicy[T])
  extends Auction[T, OpenBidAuction[T]] with PriceQuoting {


  def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
    quotingPolicy(orderBook, request)
  }

  def insert(order: LimitBidOrder[T]): OpenBidAuction[T] = {
    new OpenBidAuction(orderBook.insert(order), pricingPolicy, quotingPolicy)
  }

  def remove(order: LimitBidOrder[T]): OpenBidAuction[T] = {
    new OpenBidAuction(orderBook.remove(order), pricingPolicy, quotingPolicy)
  }

  def clear: ClearResult[T, OpenBidAuction[T]] = {
    pricingPolicy(orderBook) match {
      case Some(price) =>
        val (pairedOrders, newOrderBook) = orderBook.takeAllMatched
        val fills = pairedOrders.map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        ClearResult(Some(fills), new OpenBidAuction(newOrderBook, pricingPolicy, quotingPolicy))
      case None => ClearResult[T, OpenBidAuction[T]](None, this)
    }
  }

}


object OpenBidAuction {

  def apply[T <: Tradable](reservation: LimitAskOrder[T], pricingPolicy: PricingPolicy[T], quotePolicy: PriceQuotePolicy[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction(orderBook.insert(reservation), pricingPolicy, quotePolicy)
  }

  /** Create a first-price, sealed-bid `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def firstPriceOpenBidAuction[T <: Tradable](reservation: LimitAskOrder[T], quotePolicy: PriceQuotePolicy[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new AskQuotePricingPolicy, quotePolicy)
  }

  /** Create a second-price, sealed-bid or Vickrey `Auction`.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def secondPriceOpenBidAuction[T <: Tradable](reservation: LimitAskOrder[T], quotePolicy: PriceQuotePolicy[T]): OpenBidAuction[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new OpenBidAuction[T](orderBook.insert(reservation), new BidQuotePricingPolicy, quotePolicy)
  }

  /** Used to create an auction with an open order book without specifying the quoting policy.
    *
    * @param reservation
    * @tparam T
    * @return
    */
  def withOpenOrderBook[T <: Tradable](reservation: LimitAskOrder[T]): WithOpenOrderBook[T] = {
    val orderBook = FourHeapOrderBook.empty[T]
    new WithOpenOrderBook[T](orderBook)
  }

  final class WithOpenOrderBook[T <: Tradable](orderBook: FourHeapOrderBook[T]) extends WithOrderBook[T](orderBook) {

    def insert(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook.insert(order))
    }

    def remove(order: LimitBidOrder[T]): WithOpenOrderBook[T] = {
      new WithOpenOrderBook[T](orderBook.remove(order))
    }

    def withQuotePolicy(quotingPolicy: PriceQuotePolicy[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook, quotingPolicy)
    }

  }

  final class WithQuotePolicy[T <: Tradable](orderBook: FourHeapOrderBook[T], quotingPolicy: PriceQuotePolicy[T])
    extends WithOrderBook[T](orderBook) {

    def receive(request: PriceQuoteRequest): Option[PriceQuote] = {
      quotingPolicy(orderBook, request)
    }

    def insert(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.insert(order), quotingPolicy)
    }

    def remove(order: LimitBidOrder[T]): WithQuotePolicy[T] = {
      new WithQuotePolicy[T](orderBook.remove(order), quotingPolicy)
    }

    def withPricingPolicy(pricingPolicy: PricingPolicy[T]): OpenBidAuction[T] = {
      new OpenBidAuction[T](orderBook, pricingPolicy, quotingPolicy)
    }

  }

}
