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
package org.economicsl.auctions.singleunit.orders

import org.economicsl.auctions.{AskOrder, Issuer, SingleUnit}
import org.economicsl.core.{Price, Tradable}


/** Class representing a single-unit order to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `AskOrder` is being issued.
  * @author davidrpugh
  * @since 0.2.0
  */
case class SingleUnitAskOrder[+T <: Tradable](issuer: Issuer, limit: Price, tradable: T)
  extends AskOrder[T] with SingleUnit[T]


/** Companion object for `SingleUnitAskOrder`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitAskOrder {

  def apply[T <: Tradable](issuer: Issuer, tradable: T): SingleUnitAskOrder[T] = {
    new SingleUnitAskOrder(issuer, Price.MinValue, tradable)
  }

}