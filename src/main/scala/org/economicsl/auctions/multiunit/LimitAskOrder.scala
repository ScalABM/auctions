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

import org.economicsl.auctions.{AskOrder, Price, Quantity, Tradable}


/** Base trait for a multi-unit limit order to sell some `Tradable`. */
trait LimitAskOrder[+T <: Tradable] extends AskOrder[T] with SinglePricePoint[T] with Divisible[T, LimitAskOrder[T]]


/** Companion object for `LimitAskOrder`.
  *
  * Provides default ordering as well as constructor for default implementation of `LimitAskOrder` trait.
  */
object LimitAskOrder {

  implicit def ordering[O <: LimitAskOrder[_ <: Tradable]]: Ordering[O] = SinglePricePoint.ordering[O]

  def apply[T <: Tradable](issuer: UUID, limit: Price, quantity: Quantity, tradable: T): LimitAskOrder[T] = {
    SinglePricePointImpl(issuer, limit, quantity, tradable)
  }

  private[this] case class SinglePricePointImpl[+T <: Tradable](issuer: UUID, limit: Price, quantity: Quantity, tradable: T)
    extends LimitAskOrder[T] {

    def withQuantity(residual: Quantity): LimitAskOrder[T] = {
      require(residual.value < quantity.value)
      copy(quantity = residual)
    }

  }

}

