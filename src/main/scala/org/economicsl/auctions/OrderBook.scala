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
package org.economicsl.auctions

import org.economicsl.auctions.messages.{NewOrder, OrderId, OrderReferenceId}
import org.economicsl.core.Tradable


/** Base trait defining the interface for all `OrderBook` implementations.
  *
  * @tparam T
  * @tparam O
  * @tparam OB
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderBook[T <: Tradable, O <: NewOrder[T], +OB <: OrderBook[T, O, OB]] {

  /** Return a new `OrderBook` containing the new order.
    *
    * @param kv
    * @return
    * @note implementation should be O(1) (i.e., constant time).
    */
  def + (kv: (OrderReferenceId, (OrderId, O))): OB

  /** Return a new `OrderBook` without a particular order.
    *
    * @param existing
    * @return
    * @note implementation should be O(1) (i.e., constant time).
    */
  def - (existing: OrderReferenceId): (OB, Option[(OrderId, O)])

}
