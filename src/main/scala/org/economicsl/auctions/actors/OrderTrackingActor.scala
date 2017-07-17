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
import org.economicsl.auctions.singleunit.OrderTracking
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


/** Mixin trait providing `OrderTracking` behavior.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderTrackingActor
    extends StackableActor
    with DiagnosticActorLogging
    with OrderTracking {

  import OrderTracking._

  /** Add any `Accepted` orders to the collection of outstanding orders; orders that have been `Canceled` are removed
    * from the collection of outstanding orders; orders that have been `Rejected` are logged for debugging.
    */
  def trackingOrders: Receive = {
    case Accepted(_, token, order, reference) =>
      outstandingOrders = outstandingOrders + (token -> (reference -> order))
    case Canceled(_, token, _, _) =>
      outstandingOrders = outstandingOrders - token
    case message: Rejected =>
      log.debug(message.toString)
  }

  var outstandingOrders: Map[Token, (Reference, Order[Tradable])]

}

