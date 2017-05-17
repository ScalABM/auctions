package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{AskPriceQuote, AskPriceQuoteRequest}


trait AskPriceQuoting[T <: Tradable, -A] {

  def receive(a: A, request: AskPriceQuoteRequest): Option[AskPriceQuote]

}
