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
package org.economicsl.auctions.messages

import org.economicsl.core.{Currency, Price, Tradable}


/** Base trait for all `Reason` implementations.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait Reason {

  def message: String

}


case object OrderNotFound extends Reason {
  val message: String = "Order not found in the order book."
}


final case class InvalidTickSize(limit: Price, tickSize: Currency) extends Reason {
  val message: String = s"Limit price of $limit is not a multiple of the tick size $tickSize."
}


final case class InvalidTradable[+T <: Tradable](actual: Tradable, required: Tradable) extends Reason {
  val message: String = s"Order tradable $actual must be the same as auction $required."
}
