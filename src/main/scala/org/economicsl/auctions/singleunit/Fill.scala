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

import org.economicsl.auctions.{Contract, Price, Quantity, Tradable}


/** Note that a Fill is also a type of Contract! */
case class Fill[T <: Tradable](askOrder: LimitAskOrder[T], bidOrder: LimitBidOrder[T], price: Price) extends Contract {

  require(askOrder.limit <= price, s"Fill price of $price, is not greater than seller's limit price of ${askOrder.limit}.")
  require(price <= bidOrder.limit,  s"Fill price of $price, is not less than buyer's limit price of ${askOrder.limit}.")

  val quantity: Quantity = askOrder.quantity min bidOrder.quantity

}
