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

import org.economicsl.auctions.Order  // auction should depend on messages!
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** A message sent from an `AuctionParticipant` to an `Auction` specifying a new order.
  *
  * @param order
  * @param orderId
  * @param senderId
  * @param timestamp
  * @tparam T
  * @author davidrpugh
  * @since 0.2.0
  */
final case class InsertOrder[+T <: Tradable](order: Order[T], orderId: OrderId, senderId: SenderId, timestamp: Timestamp)
  extends Message
