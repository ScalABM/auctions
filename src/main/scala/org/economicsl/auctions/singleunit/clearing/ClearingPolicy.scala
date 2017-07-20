package org.economicsl.auctions.singleunit.clearing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.auctions.Fill
import org.economicsl.core.{Price, Tradable}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait ClearingPolicy[T <: Tradable, A <: Auction[T, A]] {
  this: A =>

  def clear: (A, Option[Stream[Fill]])

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait DiscriminatoryClearingPolicy[T <: Tradable, A <: Auction[T, A]]
    extends ClearingPolicy[T, A] {
  this: A =>

  def clear: (A, Option[Stream[Fill]]) = {

    @annotation.tailrec
    def loop(pricingPolicy: PricingPolicy[T])(fills: Stream[Fill], ob: FourHeapOrderBook[T]): (A, Option[Stream[Fill]]) = {
      val currentPrice = pricingPolicy(ob)
      val (residualOrderBook, topMatch) = ob.splitAtTopMatch
      topMatch match {
        case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
          val fill = currentPrice.map(price => Fill.fromOrders(askOrder, bidOrder, price))
          loop(pricingPolicy)(fill.fold(fills)(_ #:: fills), residualOrderBook)
        case None =>
          val results = if (fills.nonEmpty) Some(fills) else None
          (withOrderBook(ob), results)
      }
    }

    loop(pricingPolicy)(Stream.empty, orderBook)

  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait UniformClearingPolicy[T <: Tradable, A <: Auction[T, A]]
    extends ClearingPolicy[T, A] {
  this: A =>

  def clear: (A, Option[Stream[Fill]]) = {
    val uniformPrice = pricingPolicy.apply(orderBook)
    uniformPrice match {
      case Some(price) =>
        val (residualOrderBook, fills) = accumulate(price)(orderBook, Stream.empty)
        val results = if (fills.nonEmpty) Some(fills) else None
        (withOrderBook(residualOrderBook), results)
      case None => (this, None)
    }
  }

  @annotation.tailrec
  private[this] def accumulate(price: Price)(ob: FourHeapOrderBook[T], fills: Stream[Fill]): (FourHeapOrderBook[T], Stream[Fill]) = {
    val (residualOrderBook, topMatch) = ob.splitAtTopMatch
    topMatch match {
      case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
        val fill = Fill.fromOrders(askOrder, bidOrder, price)
        accumulate(price)(residualOrderBook, fill #:: fills)
      case None =>
        (ob, fills)
    }
  }

}