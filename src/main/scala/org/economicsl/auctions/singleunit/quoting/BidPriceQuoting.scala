package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{BidPriceQuote, BidPriceQuoteRequest}


trait BidPriceQuoting[T <: Tradable, -A] {

  def receive(a: A, request: BidPriceQuoteRequest): Option[BidPriceQuote]

}
