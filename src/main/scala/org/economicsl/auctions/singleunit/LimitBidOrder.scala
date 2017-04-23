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
package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{BidOrder, Price, Tradable}


/** An order to buy a single-unit of a tradable at a price less than or equal to the limit price. */
class LimitBidOrder[+T <: Tradable](val issuer: UUID, val limit: Price, val tradable: T) extends BidOrder[T] with SingleUnit[T]


object LimitBidOrder {

  implicit def ordering[O <: LimitBidOrder[_ <: Tradable]]: Ordering[O] = SingleUnit.ordering[O]

  def apply[T <: Tradable](issuer: UUID, limit: Price, tradable: T): LimitBidOrder[T] = {
    new LimitBidOrder(issuer, limit, tradable)
  }

}
