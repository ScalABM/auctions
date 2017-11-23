package org.economicsl.auctions

import org.economicsl.auctions.messages._
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamper


abstract class Auction[O <: NewOrder[_ <: Tradable], A <: Auction[O, A]](
  orderBook: OrderBook[O],
  pricingPolicy: PricingPolicy[OrderBook[O]])
    extends OrderReferenceIdGenerator
    with Timestamper {
  this: A =>

  /** Unique identifier for an `Auction`. */
  def auctionId: AuctionId

  def insert(message: O): (A, Either[NewOrderRejected, NewOrderAccepted])

  def cancel(message: CancelOrder): (A, Either[CancelOrderRejected, CancelOrderAccepted])

  def withOrderBook(updated: OrderBook[O]): A

}
