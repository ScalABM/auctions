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

import org.economicsl.auctions.{Price, Quantity, SinglePricePoint, Tradable}


/** An order to sell multiple units of a tradable at a per-unit price greater than or equal to the limit price.
  *
  * @param issuer
  * @param limit
  * @param quantity
  * @param tradable
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
class LimitAskOrder[+T <: Tradable](val issuer: UUID, val limit: Price, val quantity: Quantity, val tradable: T)
  extends AskOrder[T] with SinglePricePoint[T]


/** Companion object for `LimitAskOrder`.
  *
  * Provides default ordering for the multi-unit `LimitAskOrder` type.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object LimitAskOrder {

  implicit def ordering[O <: LimitAskOrder[_ <: Tradable]]: Ordering[(UUID, O)] = {
    Ordering.by{case (uuid, order) => (order.limit, uuid) }
  }

  def apply[T <: Tradable](issuer: UUID, limit: Price, quantity: Quantity, tradable: T): LimitAskOrder[T] = {
    new LimitAskOrder[T](issuer, limit, quantity, tradable)
  }

}

