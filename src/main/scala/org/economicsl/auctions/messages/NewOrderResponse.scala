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

import org.economicsl.core.util.Timestamp


/** Base trait for all `NewOrderResponse` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait NewOrderResponse extends Message {

  /** Unique identifier of the new order. */
  def orderId: OrderId

}


/** Message used to indicate that a previously submitted order was accepted.
  *
  * @param orderId the unique (to the `AuctionParticipant`) identifier of the accepted order.
  * @param orderRefId the unique (to the auction) reference number assigned to the order at the time of receipt.
  * @param senderId
  * @param timestamp
  * @author davidrpugh
  * @since 0.2.0
  */
final case class NewOrderAccepted(
  orderId: OrderId,
  orderRefId: OrderReferenceId,
  senderId: SenderId,
  timestamp: Timestamp)
    extends NewOrderResponse


/** Message used to indicate that a previously submitted order was rejected.
  *
  * @param orderId
  * @param senderId
  * @param timestamp
  * @author davidrpugh
  * @since 0.2.0
  */
final case class NewOrderRejected(
  orderId: OrderId,
  reason: Reason,
  senderId: SenderId,
  timestamp: Timestamp)
    extends NewOrderResponse