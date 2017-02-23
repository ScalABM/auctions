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
package org.economicsl.auctions

import java.util.UUID


/** Base trait for a market order to buy a particular `Tradable`. */
trait MarketBidOrder extends LimitBidOrder {

  /** An issuer of a `MarketBidOrder` is willing to pay any finite price. */
  val limit: Price = Price.MaxValue

}

/** Companion object for `MarketBidOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `MarketBidOrder` trait.
  */
object MarketBidOrder {

  def apply(issuer: UUID, quantity: Quantity, tradable: Tradable): MarketBidOrder with SinglePricePoint = {
    SinglePricePointImpl(issuer, quantity, tradable)
  }

  def apply(issuer: UUID, tradable: Tradable): MarketBidOrder with SingleUnit = {
    SingleUnitImpl(issuer, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, quantity: Quantity, tradable: Tradable)
    extends MarketBidOrder with SinglePricePoint

  private[this] case class SingleUnitImpl(issuer: UUID, tradable: Tradable) extends MarketBidOrder with SingleUnit

}
