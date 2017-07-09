package org.economicsl.auctions.singleunit

import org.economicsl.auctions.singleunit.clearing.ClearingPolicy
import org.economicsl.auctions.{ClearResult, Reference, ReferenceGenerator, Token}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.Tradable


trait Auction2[T <: Tradable, A <: Auction2[T, A]]
    extends ReferenceGenerator {
  this: A with ClearingPolicy[T, A] =>

  import AuctionParticipant._

  /** Create a new instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    * except the `order`.
    *
    * @param reference the unique identifier for the order that should be removed.
    * @return an instance of type class `A` whose order book contains all previously submitted `BidOrder` instances
    *         except the `order`.
    */
  def cancel(reference: Reference): (A, Option[Canceled]) = {
    val (residualOrderBook, kv) = orderBook.remove(reference)
    kv match {
      case Some((token, removedOrder)) =>
        val canceled = Canceled(removedOrder.issuer, token)
        (withOrderBook(residualOrderBook), Some(canceled))
      case None =>
        (this, None)
    }
  }

  /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
    *
    * @return an instance of `ClearResult` class containing an optional collection of `Fill` instances as well as an
    *         instance of the type class `A` whose `orderBook` contains all previously submitted but unmatched
    *         `AskOrder` and `BidOrder` instances.
    */
  def clear: ClearResult[A]

  /** Create a new instance of type class `A` whose order book contains an additional `BidOrder`.
    *
    * @param kv a mapping between a unique (to the auction participant) `Token` and the `BidOrder` that should be
    *           entered into the `orderBook`.
    * @return a `Tuple` whose first element is a unique `Reference` identifier for the inserted `BidOrder` and whose
    *         second element is an instance of type class `A` whose order book contains all submitted `BidOrder`
    *         instances.
    */
  def insert(kv: (Token, Order[T])): (A, Either[Rejected, Accepted]) = {
    val (_, order) = kv
    if (order.limit.value % tickSize > 0) {
      val reason = s"Limit price of ${order.limit} is not a multiple of the tick size $tickSize"
      val rejected = Rejected(order.issuer, reason)
      (this, Left(rejected))
    } else {
      val reference = randomReference()
      val accepted = Accepted(order.issuer, reference)
      val updatedOrderBook = orderBook.insert(reference -> kv)
      (withOrderBook(updatedOrderBook), Right(accepted))
    }
  }

  def tickSize: Long

  protected def withOrderBook(orderBook: FourHeapOrderBook[T]): A

  protected def orderBook: FourHeapOrderBook[T]

  protected def pricingPolicy: PricingPolicy[T]

}
