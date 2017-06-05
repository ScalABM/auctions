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


/** An order to buy multiple units of a tradable at a per-unit price less than or equal to the limit price.
  *
  * @param issuer
  * @param limit
  * @param quantity
  * @param tradable
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class LimitBidOrder[+T <: Tradable](issuer: UUID, limit: Price, quantity: Quantity, tradable: T)
  extends BidOrder[T] {

  def withQuantity(quantity: Quantity): LimitBidOrder[T] = {
    copy(quantity = quantity)
  }

}


/** Companion object for `LimitBidOrder`.
  *
  * Provides default ordering as well as constructor for default implementation of `LimitBidOrder` trait.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object LimitBidOrder {

  implicit def ordering[O <: LimitBidOrder[_ <: Tradable]]: Ordering[O] = SinglePricePoint.ordering[O]

}

