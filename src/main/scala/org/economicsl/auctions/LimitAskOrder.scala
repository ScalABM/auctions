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


/** Base trait for a limit order to sell some `Tradable`. */
trait LimitAskOrder extends AskOrder with SinglePricePoint


/** Companion object for `LimitAskOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `LimitAskOrder` trait.
  */
object LimitAskOrder {

  implicit def ordering[O <: LimitAskOrder]: Ordering[O] = SinglePricePoint.ordering[O]

  def apply(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable): LimitAskOrder = {
    SinglePricePointImpl(issuer, limit, quantity, tradable)
  }

  def apply(issuer: UUID, limit: Price, tradable: Tradable): LimitAskOrder with SingleUnit = {
    SingleUnitImpl(issuer, limit, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable)
    extends LimitAskOrder

  private[this] case class SingleUnitImpl(issuer: UUID, limit: Price, tradable: Tradable)
    extends LimitAskOrder with SingleUnit

}

