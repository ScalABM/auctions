package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.quotes.{PriceQuote, PriceQuoteRequest, Quote, QuoteRequest}


sealed trait Quoting[-R <: QuoteRequest, +Q <: Quote] {

  def receive(request: R): Option[Q]

}

trait PriceQuoting extends Quoting[PriceQuoteRequest, PriceQuote]

