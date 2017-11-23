package org.economicsl.auctions.singleunit.clearing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
<<<<<<< Updated upstream
import org.economicsl.auctions.singleunit.pricing.SingleUnitPricingPolicy
import org.economicsl.auctions.singleunit.Auction
=======
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.SingleUnitAuction
>>>>>>> Stashed changes
import org.economicsl.auctions.SpotContract
import org.economicsl.core.{Price, Tradable}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait ClearingPolicy[T <: Tradable, A <: SingleUnitAuction[T, A]] {
  this: A =>

  def clear: (A, Option[Stream[SpotContract]])

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait DiscriminatoryClearingPolicy[T <: Tradable, A <: SingleUnitAuction[T, A]]
    extends ClearingPolicy[T, A] {
  this: A =>

  def clear: (A, Option[Stream[SpotContract]]) = {

    @annotation.tailrec
    def loop(pricingPolicy: SingleUnitPricingPolicy[T])(contracts: Stream[SpotContract], ob: FourHeapOrderBook[T]): (A, Option[Stream[SpotContract]]) = {
      val currentPrice = pricingPolicy(ob)
      val (residualOrderBook, topMatch) = ob.splitAtTopMatch
      topMatch match {
        case Some(((_, (_, offer)), (_, (_, bid)))) =>
          val fill = currentPrice.map(price => SpotContract.fromSingleUnitOrders(bid, offer, price))
          loop(pricingPolicy)(fill.fold(contracts)(_ #:: contracts), residualOrderBook)
        case None =>
          val results = if (contracts.nonEmpty) Some(contracts) else None
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
trait UniformClearingPolicy[T <: Tradable, A <: SingleUnitAuction[T, A]]
    extends ClearingPolicy[T, A] {
  this: A =>

  def clear: (A, Option[Stream[SpotContract]]) = {
    val uniformPrice = pricingPolicy.apply(orderBook)
    uniformPrice match {
      case Some(price) =>
        val (residualOrderBook, contracts) = accumulate(price)(orderBook, Stream.empty)
        val results = if (contracts.nonEmpty) Some(contracts) else None
        (withOrderBook(residualOrderBook), results)
      case None => (this, None)
    }
  }

  @annotation.tailrec
  private[this] def accumulate(price: Price)(ob: FourHeapOrderBook[T], contracts: Stream[SpotContract]): (FourHeapOrderBook[T], Stream[SpotContract]) = {
    val (residualOrderBook, topMatch) = ob.splitAtTopMatch
    topMatch match {
      case Some(((_, (_, offer)), (_, (_, bid)))) =>
        val fill = SpotContract.fromSingleUnitOrders(bid, offer, price)
        accumulate(price)(residualOrderBook, fill #:: contracts)
      case None =>
        (ob, contracts)
    }
  }

}