package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.quotes.{Quote, QuoteRequest}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.core.Tradable


trait Quoting[T <: Tradable, -R <: QuoteRequest[T], +Q <: Quote[T]] {
  this: Auction[T] =>

  def receive(request: R): Q = {
    quotingPolicy.apply(orderBook, request)
  }

  def quotingPolicy: QuotingPolicy[T, R, Q]

}
