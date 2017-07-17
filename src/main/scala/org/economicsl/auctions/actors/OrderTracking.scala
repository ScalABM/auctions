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
package org.economicsl.auctions.actors

import akka.actor.DiagnosticActorLogging
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


/** Mixin trait providing `OrderTracking` behavior.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderTracking {
  this: StackableActor with DiagnosticActorLogging =>

  import OrderTracking._

  /** Add any `Accepted` orders to the collection of outstanding orders; orders that have been `Canceled` are removed
    * from the collection of outstanding orders; orders that have been `Rejected` are logged for debugging.
    */
  def trackingOrders: Receive = {
    case Accepted(_, token, order, reference) =>
      outstandingOrders = outstandingOrders + (token -> (reference -> order))
    case message: Rejected =>
      log.debug(message.toString)
    case Canceled(_, token, _) =>
      outstandingOrders = outstandingOrders - token
  }

  /* Any actor mixing in `OrderTracking` needs to keep track of outstanding orders. */
  var outstandingOrders: Map[Token, (Reference, Order[Tradable])]

}


object OrderTracking {

  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Timestamp, token: Token, order: Order[Tradable], reference: Reference)


  /** Message used to indicate that a previously submitted order has been rejected.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Rejected(timestamp: Timestamp, token: Token, reason: Throwable)


  /** Message used to indicate that a previously accepted order has been canceled.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Canceled(timestamp: Timestamp, token: Token, reason: String)

}
