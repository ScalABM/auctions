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

import java.util.UUID

import akka.actor.DiagnosticActorLogging
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


/** Mixin trait for providing `OrderTracker` behavior.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderTracking
    extends DiagnosticActorLogging {
  this: StackableActor =>

  import OrderTracking._

  /** An `OrderTracker` should add any `Accepted` orders to its collection of outstanding orders; orders that have been
    * `Canceled` should be removed from the collection of outstanding orders; orders that have been `Rejected` should
    * be logged for reference.
    */
  def trackingOrders: Receive = {
    case Accepted(_, token, order, reference) =>
      log.info(order.toString)
      outstandingOrders = outstandingOrders + (token -> (reference, order))
    case message: Rejected =>
      log.debug(message.toString)
    case message @ Canceled(_, token, _) =>
      log.info(message.toString)
      outstandingOrders = outstandingOrders - token
  }

  /* An `OrderTracker` needs to keep track of outstanding orders. */
  protected var outstandingOrders: Map[Token, (Reference, Order[Tradable])] = Map.empty

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
  final case class Accepted(timestamp: Long, token: Token, order: Order[Tradable], reference: UUID)


  /** Message used to indicate that a previously submitted order has been rejected.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Rejected(timestamp: Long, token: Token, reason: Throwable)


  /** Message used to indicate that a previously accepted order has been canceled or reduced.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Canceled(timestamp: Long, token: Token, reason: String)

}
