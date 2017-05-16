package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.quotes._


sealed trait Quoting[-R <: QuoteRequest, +Q <: Quote] {

  def receive(request: R): Option[Q]

}

trait AskPriceQuoting extends Quoting[AskPriceQuoteRequest, AskPriceQuote]

trait BidPriceQuoting extends Quoting[BidPriceQuoteRequest, BidPriceQuote]

trait PriceQuoting extends Quoting[PriceQuoteRequest, PriceQuote]

trait SpreadQuoting extends Quoting[SpreadQuoteRequest, SpreadQuote]



