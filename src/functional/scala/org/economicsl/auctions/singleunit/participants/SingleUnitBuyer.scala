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
package org.economicsl.auctions.singleunit.participants

import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.messages._
import org.economicsl.core.{Price, Tradable}


/** Class used to model an auction participant that issues `NewSingleUnitBidOrder`.
  *
  * @param participantId
  * @param issuedOrders
  * @param outstandingOrders
  * @param valuations
  * @author davidrpugh
  * @since 0.2.0
  */
class SingleUnitBuyer private(
  val participantId: SenderId,
  val issuedOrders: Map[OrderId, NewOrder[Tradable]],
  val outstandingOrders: Map[OrderId, (OrderReferenceId, NewOrder[Tradable])],
  val valuations: Map[Tradable, Price])
    extends SingleUnitAuctionParticipant {

  /** Returns a new `AuctionParticipant` that has observed the `AuctionDataResponse`.
    *
    * @param auctionDataResponse
    * @return
    */
  def handle[T <: Tradable](auctionDataResponse: AuctionDataResponse[T]): SingleUnitAuctionParticipant = {
    ???
  }

  /** Issues an order for a particular tradable.
    *
    * @param protocol
    * @tparam T
    * @return
    * @note
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitBuyer, SingleUnitBid[T])] = {
    val limit = valuations(protocol.tradable)
    val orderId = randomOrderId()
    val timestamp = currentTimeMillis()
    val issuedOrder = SingleUnitBid(limit, orderId, participantId, timestamp, protocol.tradable)
    val updated = issuedOrders + (orderId -> issuedOrder)
    Some((withIssuedOrders(updated), issuedOrder))
  }

  /** Request auction data given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def requestAuctionData[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitBuyer, (OrderId, AuctionDataRequest[T]))] = {
    None
  }

  /** Creates a new `SingleUnitBuyer` with an `updated` collection of outstanding orders. */
  protected def withIssuedOrders(updated: Map[OrderId, NewOrder[Tradable]]): SingleUnitBuyer = {
    new SingleUnitBuyer(participantId, updated, outstandingOrders, valuations)
  }

  /** Creates a new `SingleUnitBuyer` with an `updated` collection of outstanding orders. */
  protected def withOutstandingOrders(updated: Map[OrderId, (OrderReferenceId, NewOrder[Tradable])]): SingleUnitBuyer = {
    new SingleUnitBuyer(participantId, issuedOrders, updated, valuations)
  }

  /** Creates a new `SingleUnitBuyer` with `updated` valuations. */
  protected def withValuations(updated: Map[Tradable, Price]): SingleUnitBuyer = {
    new SingleUnitBuyer(participantId, issuedOrders, outstandingOrders, updated)
  }

}


/** Companion object for `SingleUnitBuyer`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitBuyer {

  def apply(participantId: SenderId, valuations: Map[Tradable, Price]): SingleUnitBuyer = {
    val issuedOrders = Map.empty[OrderId, NewOrder[Tradable]]
    val outstandingOrders = Map.empty[OrderId, (OrderReferenceId, NewOrder[Tradable])]
    new SingleUnitBuyer(participantId, issuedOrders, outstandingOrders, valuations)
  }

  def apply(valuations: Map[Tradable, Price]): SingleUnitBuyer = {
    val participantId = UUID.randomUUID()
    val issuedOrders = Map.empty[OrderId, NewOrder[Tradable]]
    val outstandingOrders = Map.empty[OrderId, (OrderReferenceId, NewOrder[Tradable])]
    new SingleUnitBuyer(participantId, issuedOrders, outstandingOrders, valuations)
  }

}
