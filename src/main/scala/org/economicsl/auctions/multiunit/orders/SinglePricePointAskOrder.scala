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
package org.economicsl.auctions.multiunit.orders

import org.economicsl.auctions.{AskOrder, Issuer, SinglePricePoint}
import org.economicsl.core.{Price, Quantity, Tradable}


/** Base trait for all multi-unit orders to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `Order` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SinglePricePointAskOrder[+T <: Tradable](
                                                     issuer: Issuer,
                                                     limit: Price,
                                                     quantity: Quantity,
                                                     tradable: T)
  extends AskOrder[T]
    with SinglePricePoint[T]


/** Companion object for SinglePricePointBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SinglePricePointAskOrder {

  def apply[T <: Tradable](issuer: Issuer, quantity: Quantity, tradable: T): SinglePricePointAskOrder[T] = {
    new SinglePricePointAskOrder(issuer, Price.MinValue, quantity, tradable)
  }

}


