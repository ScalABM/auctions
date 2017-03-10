/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions.multiunit

import java.util.UUID

import org.economicsl.auctions.{Price, Quantity, Tradable}


/** Base trait for a market order to buy a particular `Tradable`. */
trait MarketBidOrder[+T <: Tradable] extends LimitBidOrder[T] {

  /** An issuer of a `MarketBidOrder` is willing to pay any finite price. */
  val limit: Price = Price.MaxValue

}


/** Companion object for `MarketBidOrder`.
  *
  * Provides constructor for default implementation of `MarketBidOrder` trait.
  */
object MarketBidOrder {

  def apply[T <: Tradable](issuer: UUID, quantity: Quantity, tradable: T): MarketBidOrder[T] = {
    SinglePricePointImpl(issuer, quantity, tradable)
  }

  private[this] case class SinglePricePointImpl[+T <: Tradable](issuer: UUID, quantity: Quantity, tradable: T)
    extends MarketBidOrder[T] {

    def withQuantity(residual: Quantity): MarketBidOrder[T] = {
      require(residual.value < quantity.value)
      copy(quantity = residual)
    }

  }

}
