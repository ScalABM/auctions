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

import org.economicsl.auctions.Order  // messages should not depend on auctions!
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Message used to indicate that a previously submitted order was accepted.
  *
  * @param order
  * @param orderId the unique (to the `AuctionParticipant`) identifier of the previously accepted order.
  * @param orderRefId the unique (to the auction) reference number assigned to the order at the time of receipt.
  * @param senderId
  * @param timestamp
  * @author davidrpugh
  * @since 0.2.0
  */
final case class Accepted(
  order: Order[Tradable],
  orderId: OrderId,
  orderRefId: OrderReferenceId,
  senderId: SenderId,
  timestamp: Timestamp)
    extends Message {

  val kv: (OrderId, (OrderReferenceId, Order[Tradable])) = orderId -> (orderRefId -> order)

}
