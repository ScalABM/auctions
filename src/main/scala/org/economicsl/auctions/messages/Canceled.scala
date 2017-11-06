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

import org.economicsl.auctions.Order
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Base trait for all canceled messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait Canceled
  extends Message {

  def order: Order[Tradable]

  def orderId: OrderId

  def reason: Reason

}


/** Message used to indicate that a previously accepted order has been canceled by its issuer.
  *
  * @param order
  * @param orderId
  * @param senderId
  * @param timestamp
  * @author davidrpugh
  * @since 0.2.0
  */
final case class CanceledByIssuer(order: Order[Tradable], orderId: OrderId, senderId: SenderId, timestamp: Timestamp)
  extends Canceled {
  val reason: Reason = IssuerRequestedCancel(order)
}
