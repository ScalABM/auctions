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

import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.{Contract, Price, Tradable}


/** Class representing a `Fill`.
  *
  * @param askOrder
  * @param bidOrder
  * @param price
  * @tparam T the type of `Tradable` for which the `Fill` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class Fill[T <: Tradable](askOrder: AskOrder[T], bidOrder: BidOrder[T], price: Price) extends Contract {

  /** By convention a `Fill` represents a liability of the buyer */
  val issuer: UUID = bidOrder.issuer

  require(askOrder.limit <= price, s"Fill price of $price, is not greater than seller's limit price of ${askOrder.limit}.")
  require(price <= bidOrder.limit,  s"Fill price of $price, is not less than buyer's limit price of ${bidOrder.limit}.")

}
