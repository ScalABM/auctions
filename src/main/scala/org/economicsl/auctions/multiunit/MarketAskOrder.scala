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
package org.economicsl.auctions.multiunit

import java.util.UUID

import org.economicsl.auctions.SinglePricePoint
import org.economicsl.core.{Price, Quantity, Tradable}


/** An order to sell a multiple units of a tradable at any positive price.
  *
  * @param issuer
  * @param quantity
  * @param tradable
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
class MarketAskOrder[+T <: Tradable](val issuer: UUID, val quantity: Quantity, val tradable: T)
  extends SinglePricePointAskOrder[T] with SinglePricePoint[T] {

  val limit: Price = Price.MinValue

}


/** Companion object for `MarketAskOrder`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object MarketAskOrder {

  def apply[T <: Tradable](issuer: UUID, quantity: Quantity, tradable: T): MarketAskOrder[T] = {
    new MarketAskOrder[T](issuer, quantity, tradable)
  }

}

