package org.economicsl.auctions

import org.economicsl.auctions.quotes.{Quote, QuoteRequest}
import org.economicsl.core.Tradable


trait QuotingPolicy[T <: Tradable] {

  def receive(request: QuoteRequest[T]): Quote

}
