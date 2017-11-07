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

import org.economicsl.auctions.messages._
import org.economicsl.core.{Price, Tradable}


/** Trait that encapsulates auction participant behavior.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait AuctionParticipant[+P <: AuctionParticipant[P]]
    extends OrderIdGenerator[P] {
  this: P =>

  /** Returns a new `AuctionParticipant` ...
    *
    * @param result
    * @return
    * @note implementation delegates to overloaded `handle` depending on whether result is `Accepted` or `Rejected`.
    */
  final def handle(result: Either[NewOrderRejected, NewOrderAccepted]): P = {
    result match {
      case Left(rejected) => handle(rejected)
      case Right(accepted) => handle(accepted)
    }
  }

  /** Returns a new `AuctionParticipant` whose outstanding orders contains the accepted order.
    *
    * @param accepted
    * @return
    */
  final def handle(accepted: NewOrderAccepted): P = {
    issuedOrders.get(accepted.orderId) match {
      case Some(order) =>
        val remainingIssuedOrders = issuedOrders - accepted.orderId
        val additionalOutstandingOrders = outstandingOrders + (accepted.orderId -> (accepted.orderRefId -> order))
        withIssuedOrders(remainingIssuedOrders).withOutstandingOrders(additionalOutstandingOrders)
      case None =>
        this
    }
  }

  /** Returns an `AuctionParticipant` whose outstanding orders do not contain the rejected order.
    *
    * @param rejected
    * @return
    */
  final def handle(rejected: NewOrderRejected): P = {
    val remainingIssuedOrders = issuedOrders - rejected.orderId
    withIssuedOrders(remainingIssuedOrders)
  }

  /** Returns a new `AuctionParticipant` whose outstanding orders no longer contains the canceled order.
    *
    * @param message
    * @return
    */
  final def handle(message: CancelOrderAccepted): P = {
    val updated = outstandingOrders - message.orderId
    withOutstandingOrders(updated)
  }

  /**
    *
    * @param message
    * @return
    * @note sub-classes may wish to override this behavior and call super.
    */
  def handle(message: CancelOrderRejected): P = {
    this
  }

  /** Returns a new `AuctionParticipant` that has observed the `AuctionDataResponse`.
    *
    * @param auctionDataResponse
    * @return
    */
  def handle[T <: Tradable](auctionDataResponse: AuctionDataResponse[T]): P

  /** Each `AuctionParticipant` needs to be uniquely identified. */
  def participantId: SenderId

  /** Each `AuctionParticipant` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(P, (OrderId, Order[T]))]

  /** Each `AuctionParticipant` needs to request auction data given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def requestAuctionData[T <: Tradable](protocol: AuctionProtocol[T]): Option[(P, (OrderId, AuctionDataRequest[T]))]

  /** An `AuctionParticipant` needs to keep track of its previously issued `Order` instances. */
  def issuedOrders: Map[OrderId, Order[Tradable]]

  /** An `AuctionParticipant` needs to keep track of its outstanding `Order` instances. */
  def outstandingOrders: Map[OrderId, (OrderReferenceId, Order[Tradable])]

  /** An `AuctionParticipant` needs to keep track of its valuations for each `Tradable`. */
  def valuations: Map[Tradable, Price]

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withIssuedOrders(updated: Map[OrderId, Order[Tradable]]): P

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withOutstandingOrders(updated: Map[OrderId, (OrderReferenceId, Order[Tradable])]): P

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withValuations(updated: Map[Tradable, Price]): P

}

