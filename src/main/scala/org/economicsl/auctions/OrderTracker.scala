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


/** Trait that encapsulates order tracking behavior. */
trait OrderTracker[A <: OrderTracker[A]] {
  this: A =>

  import OrderTracker._

  final def trackOrders(accepted: Accepted): A = {
      val updated = outstandingOrders + accepted.kv
      withOutstandingOrders(updated)
  }

  /**
    *
    * @param rejected
    * @return
    * @note sub-classes should almost certainly override this method and call super.
    */
  def trackOrders(rejected: Rejected): A = {
    this  // todo is this the most appropriate default behavior?
  }

  final def trackOrders(canceled: Canceled): A = {
    val updated = outstandingOrders - canceled.token
    withOutstandingOrders(updated)
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Contract)]): A

  protected val outstandingOrders: Map[Token, (Reference, Contract)]

}


object OrderTracker {

  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Timestamp, token: Token, order: Contract, reference: Reference) {

    val kv: (Token, (Reference, Contract)) = token -> (reference -> order)

  }


  /** Base trait for all canceled messages.
    *
    * @author davidrpugh
    * @since 0.2.0
    */
  trait Canceled {

    def timestamp: Timestamp

    def token: Token

    def order: Contract

    def reason: Reason

  }

  /** Message used to indicate that a previously accepted order has been canceled by its issuer.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class CanceledByIssuer(timestamp: Timestamp, token: Token, order: Contract) extends Canceled {
    val reason: Reason = IssuerRequestedCancel(order)
  }


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

  final case class IssuerRequestedCancel(order: Contract) extends Reason {
    val message: String = s"Issuer ${order.issuer} requested cancel."
  }

  final case class InvalidTickSize(order: Contract with SinglePricePoint[Tradable], tickSize: Long) extends Reason {
    val message: String = s"Limit price of ${order.limit} is not a multiple of the tick size $tickSize"
  }

  final case class InvalidTradable(order: Contract with SinglePricePoint[Tradable], tradable: Tradable) extends Reason {
    val message: String = s"Order tradable ${order.tradable} must be the same as auction $tradable."
  }

}
