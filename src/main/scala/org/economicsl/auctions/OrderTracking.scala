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

import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


trait OrderTracking {

  protected def outstandingOrders: Map[Token, (Reference, Contract)]

}


object OrderTracking {

  /** Message used to indicate that a previously accepted order has been canceled.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Canceled(timestamp: Timestamp, token: Token, order: Contract, reason: Reason)


  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Timestamp, token: Token, order: Contract, reference: Reference)


  /** Message used to indicate that a previously submitted order has been rejected.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Rejected(timestamp: Timestamp, token: Token, order: Contract, reason: Reason)

  sealed trait Reason {

    def message: String

  }

  final case class InvalidTickSize(order: Contract with SinglePricePoint[Tradable], tickSize: Long) extends Reason {
    val message: String = s"Limit price of ${order.limit} is not a multiple of the tick size $tickSize"
  }

}
