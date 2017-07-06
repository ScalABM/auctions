package org.economicsl.auctions.singleunit.clearing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.{Auction, AuctionLike}
import org.economicsl.auctions.{ClearResult, Fill}
import org.economicsl.core.{Price, Tradable}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait ClearingPolicy[T <: Tradable, A <: Auction[T]] {
  this: AuctionLike[T, A] =>

  def clear(a: A): ClearResult[A]

  protected def withOrderBook(auction: A, orderBook: FourHeapOrderBook[T]): A

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait DiscriminatoryClearingPolicy[T <: Tradable, A <: Auction[T]]
    extends ClearingPolicy[T, A] {
  this: AuctionLike[T, A] =>

  def clear(a: A): ClearResult[A] = {

    @annotation.tailrec
    def loop(pricingPolicy: PricingPolicy[T])(fills: Stream[Fill], ob: FourHeapOrderBook[T]): ClearResult[A] = {
      val currentPrice = pricingPolicy(ob)
      val (residualOrderBook, topMatch) = ob.splitAtTopMatch
      topMatch match {
        case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
          val fill = currentPrice.map(price => Fill.fromOrders(askOrder, bidOrder, price))
          loop(pricingPolicy)(fill.fold(fills)(_ #:: fills), residualOrderBook)
        case None =>
          val results = if (fills.nonEmpty) Some(fills) else None
          ClearResult(results, withOrderBook(a, ob))
      }
    }

    loop(a.pricingPolicy)(Stream.empty, a.orderBook)

  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait UniformClearingPolicy[T <: Tradable, A <: Auction[T]]
    extends ClearingPolicy[T, A] {
  this: AuctionLike[T, A] =>

  def clear(a: A): ClearResult[A] = {
    val uniformPrice = a.pricingPolicy.apply(a.orderBook)
    uniformPrice match {
      case Some(price) =>
        val (fills, residualOrderBook) = accumulate(price)(Stream.empty, a.orderBook)
        val results = if (fills.nonEmpty) Some(fills) else None
        ClearResult(results, withOrderBook(a, residualOrderBook))
      case None => ClearResult(None, a)
    }
  }

  @annotation.tailrec
  private[this] def accumulate(price: Price)(fills: Stream[Fill], ob: FourHeapOrderBook[T]): (Stream[Fill], FourHeapOrderBook[T]) = {
    val (residualOrderBook, topMatch) = ob.splitAtTopMatch
    topMatch match {
      case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
        val fill = Fill.fromOrders(askOrder, bidOrder, price)
        accumulate(price)(fill #:: fills, residualOrderBook)
      case None =>
        (fills, ob)
    }
  }

}