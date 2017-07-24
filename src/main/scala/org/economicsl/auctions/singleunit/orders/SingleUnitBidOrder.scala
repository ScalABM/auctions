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

import org.economicsl.auctions.{BidOrder, Issuer, SingleUnit}
import org.economicsl.core.{Price, Tradable}


/** Class representing a single-unit orders to buy a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `BidOrder` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SingleUnitBidOrder[+T <: Tradable](issuer: Issuer, limit: Price, tradable: T)
  extends BidOrder[T] with SingleUnit[T]


/** Companion object for SingleUnitBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitBidOrder {

  def apply[T <: Tradable](issuer: Issuer, tradable: T): SingleUnitBidOrder[T] = {
    new SingleUnitBidOrder(issuer, Price.MinValue, tradable)
  }

}