package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.quotes.{SpreadQuote, SpreadQuoteRequest}


trait SpreadQuoting[T <: Tradable, -A] {

  def receive(a: A, request: SpreadQuoteRequest): Option[SpreadQuote]

}



