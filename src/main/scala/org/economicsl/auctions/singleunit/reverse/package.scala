package org.economicsl.auctions.singleunit

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.AskOrder
import org.economicsl.auctions.singleunit.pricing.PricingPolicy


package object reverse {

  class ReverseAuctionLikeOps[T <: Tradable, A](a: A)(implicit ev: ReverseAuctionLike[T, A]) {

    def insert(order: AskOrder[T]): A = ev.insert(a, order)

    def remove(order: AskOrder[T]): A = ev.remove(a, order)

    def clear: ClearResult[T, A] = ev.clear(a)

    protected val orderBook: FourHeapOrderBook[T] = ev.orderBook(a)

    protected val pricingPolicy: PricingPolicy[T] = ev.pricingPolicy(a)

  }

  class OpenReverseAuctionLikeOps[T <: Tradable, A](a: A)(implicit ev: OpenReverseAuctionLike[T, A])
    extends ReverseAuctionLikeOps[T, A](a)(ev) {

    def receive(request: AskPriceQuoteRequest): Option[AskPriceQuote] = ev.receive(a, request)

  }

}
