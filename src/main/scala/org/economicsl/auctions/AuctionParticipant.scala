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

import org.economicsl.core.{Price, Tradable}
import org.economicsl.core.util.Timestamp


/** Trait that encapsulates auction participant behavior.
  *
  * @tparam A
  * @author davidrpugh
  * @since 0.2.0
  */
trait AuctionParticipant[+A <: AuctionParticipant[A]]
    extends TokenGenerator {
  this: A =>

  import AuctionParticipant._

  /** Returns a new `AuctionParticipant` whose outstanding orders contains the accepted order.
    *
    * @param accepted
    * @return
    */
  final def handle(accepted: Accepted): A = {
    val updated = outstandingOrders + accepted.kv
    withOutstandingOrders(updated)
  }

  /** Returns an `AuctionParticipant` whose outstanding orders do not contain the rejected order.
    *
    * @param rejected
    * @return
    * @note sub-classes may want to override this method and call super.
    */
  def handle(rejected: Rejected): A = {
    withOutstandingOrders(outstandingOrders)
  }

  /** Returns a new `AuctionParticipant` whose outstanding orders no longer contains the canceled order.
    *
    * @param canceled
    * @return
    */
  final def handle(canceled: Canceled): A = {
    val updated = outstandingOrders - canceled.token
    withOutstandingOrders(updated)
  }

  /** Each `AuctionParticipant` needs to be uniquely identified. */
  def issuer: Issuer

  /** Each `AuctionParticipant` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return a `Tuple2` whose first element contains a `Token` that uniquely identifies an `Order` and whose second
    *         element is an `Order`.
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): (A, (Token, Order[T]))

  /** An `AuctionParticipant` needs to keep track of its previously issued `Order` instances. */
  protected def outstandingOrders: Map[Token, (Reference, Order[Tradable])]

  /** An `AuctionParticipant` needs to keep track of its valuations for each `Tradable`. */
  protected def valuations: Map[Tradable, Price]

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Order[Tradable])]): A

}


/** Companion object for the `AuctionParticipant` trait.
  *
  * Contains various messages that the `AuctionParticipant` needs to handle.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object AuctionParticipant {

  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Timestamp, token: Token, order: Order[Tradable], reference: Reference) {

    val kv: (Token, (Reference, Order[Tradable])) = token -> (reference -> order)

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
  final case class CanceledByIssuer(timestamp: Timestamp, token: Token, order: Order[Tradable]) extends Canceled {
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
  final case class Rejected(timestamp: Timestamp, token: Token, order: Order[Tradable], reason: Reason)

  sealed trait Reason {

    def message: String

  }


  final case class IssuerRequestedCancel(order: Order[Tradable]) extends Reason {
    val message: String = s"Issuer ${order.issuer} requested cancel."
  }


  final case class InvalidTickSize[+T <: Tradable](
                                                    order: Order[T] with SinglePricePoint[T],
                                                    protocol: AuctionProtocol[T])
    extends Reason {
    val message: String = s"Limit price of ${order.limit} is not a multiple of the tick size ${protocol.tickSize}."
  }


  final case class InvalidTradable[+T <: Tradable](
                                                    order: Order[T] with SinglePricePoint[T],
                                                    protocol: AuctionProtocol[T])
    extends Reason {
    val message: String = s"Order tradable ${order.tradable} must be the same as auction ${protocol.tradable}."
  }

}

