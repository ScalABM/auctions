/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions

import java.util.UUID


/** Base trait defining an order for a particular tradable object. */
sealed trait Order[+T <: Tradable] {

  /** Some kind of unique identifier of the market participant that issued the order. */
  def issuer: UUID

  /** The type of tradable for which the order has been issued. */
  def tradable: T

}


/** Base trait for an order to sell some `Tradable`. */
trait AskOrder[+T <: Tradable] extends Order[T]


/** Base trait for an order to buy some `Tradable`. */
trait BidOrder[+T <: Tradable] extends Order[T]

