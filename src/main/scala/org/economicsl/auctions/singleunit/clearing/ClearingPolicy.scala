/*
Copyright 2016 David R. Pugh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit.clearing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.SingleUnitAuction
import org.economicsl.auctions.SpotContract
import org.economicsl.core.{Price, Tradable}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait ClearingPolicy[T <: Tradable, +A <: SingleUnitAuction[T, A]] {
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
    def loop(pricingPolicy: PricingPolicy[T])(contracts: Stream[SpotContract], ob: FourHeapOrderBook[T]): (A, Option[Stream[SpotContract]]) = {
      val currentPrice = pricingPolicy(ob)
      val (residualOrderBook, topMatch) = ob.splitAtTopMatch
      topMatch match {
        case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
          val fill = currentPrice.map(price => SpotContract.fromOrders(askOrder, bidOrder, price))
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
      case Some(((_, (_, askOrder)), (_, (_, bidOrder)))) =>
        val fill = SpotContract.fromOrders(askOrder, bidOrder, price)
        accumulate(price)(residualOrderBook, fill #:: contracts)
      case None =>
        (ob, contracts)
    }
  }

}