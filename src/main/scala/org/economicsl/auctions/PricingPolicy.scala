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

import org.economicsl.auctions.messages.NewOrder
import org.economicsl.core.{Price, Tradable}


/** Base trait for all `PricingPolicy` implementations.
  *
  * @tparam OB
  * @author davidrpugh
  * @since 0.2.0
  * @note A `PricingPolicy` should map the state of an `Auction` to an optional price.  Currently all `Auction` state
  *       is encapsulated by an `OrderBook`; in the future this might not be the case.
  */
trait PricingPolicy[T <: Tradable, O <: NewOrder[T], OB <: OrderBook[O, OB]] extends ((OB) => Option[Price])