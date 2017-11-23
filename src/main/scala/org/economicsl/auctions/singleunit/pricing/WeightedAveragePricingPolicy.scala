/*
Copyright (c) 2017 KAPSARC

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
package org.economicsl.auctions.singleunit.pricing

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.core.{Price, Tradable}


/** Class implementing a weighted average pricing policy.
  *
  * @param weight
  * @tparam T
  * @author davidrpugh
  * @since 0.1.0
  */
class WeightedAveragePricingPolicy[T <: Tradable](weight: Double) extends SingleUnitPricingPolicy[T] {
  require(0 <= weight && weight <= 1.0)  // individual rationality requirement!

  def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
    orderBook.askPriceQuote.flatMap(askPrice => orderBook.bidPriceQuote.map(bidPrice=> average(weight)(bidPrice, askPrice)))
  }

  private[this] def average(k: Double)(bid: Price, ask: Price): Price = {
    val weightedAverage = k * bid.value.toDouble + (1 - k) * ask.value.toDouble
    Price(weightedAverage.round)  // be mindful of possible overflow!
  }

}


object WeightedAveragePricingPolicy {

  def apply[T <: Tradable](weight: Double): WeightedAveragePricingPolicy[T] = {
    new WeightedAveragePricingPolicy(weight)
  }

}
