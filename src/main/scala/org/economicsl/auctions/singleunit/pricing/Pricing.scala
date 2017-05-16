package org.economicsl.auctions.singleunit.pricing

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.{ClearResult, Fill}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook


sealed trait Pricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }] {

  def clear(a: A): ClearResult[T, A]

  protected def withOrderBook(auction: A, orderBook: FourHeapOrderBook[T]): A

}


trait DiscriminatoryPricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }]
  extends Pricing[T, A] {

  def clear(a: A): ClearResult[T, A] = {

    @annotation.tailrec
    def loop(pricingPolicy: PricingPolicy[T])(fills: Stream[Fill[T]], ob: FourHeapOrderBook[T]): ClearResult[T, A] = {
      val currentPrice = pricingPolicy(ob)
      val (bestMatch, residualOrderBook) = ob.takeBestMatched
      bestMatch match {
        case Some((askOrder, bidOrder)) =>
          val fill = currentPrice.map(price => Fill(askOrder, bidOrder, price))
          loop(pricingPolicy)(fill.fold(fills)(_ #:: fills), residualOrderBook)
        case None =>
          val results = if (fills.nonEmpty) Some(fills) else None
          ClearResult(results, withOrderBook(a, ob))
      }
    }

    loop(a.pricingPolicy)(Stream.empty, a.orderBook)

  }

}


trait UniformPricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }]
  extends Pricing[T, A] {

  def clear(a: A): ClearResult[T, A] = { // todo this should be refactored to be tail recursive!
    a.pricingPolicy.apply(a.orderBook) match {
      case Some(price) =>
        val (matchedOrders, residualOrderBook) = a.orderBook.takeAllMatched
        val fills = matchedOrders.map{ case (askOrder, bidOrder) => Fill(askOrder, bidOrder, price) }
        ClearResult(Some(fills), withOrderBook(a, residualOrderBook))
      case None => ClearResult(None, a)
    }
  }

}