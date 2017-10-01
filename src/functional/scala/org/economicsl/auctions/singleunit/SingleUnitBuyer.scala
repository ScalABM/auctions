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
package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orders.SingleUnitBidOrder
import org.economicsl.core.{Price, Tradable}


/** Class used to model an auction participant that issues `SingleUnitBidOrder`.
  *
  * @param issuer
  * @param outstandingOrders
  * @param valuations
  * @author davidrpugh
  * @since 0.2.0
  */
class SingleUnitBuyer private(
  val issuer: Issuer,
  val outstandingOrders: Map[Token, (Reference, Order[Tradable])],
  val valuations: Map[Tradable, Price])
    extends SingleUnitAuctionParticipant {

  /** Issues an order for a particular tradable.
    *
    * @param protocol
    * @tparam T
    * @return
    * @note
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitBuyer, (Token, SingleUnitBidOrder[T]))] = {
    val valuation = valuations(protocol.tradable)
    Some((this, (randomToken(), SingleUnitBidOrder(issuer, valuation, protocol.tradable))))
  }

  /** Creates a new `SingleUnitBuyer` with an `updated` collection of outstanding orders. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Order[Tradable])]): SingleUnitBuyer = {
    new SingleUnitBuyer(issuer, updated, valuations)
  }

  /** Creates a new `SingleUnitBuyer` with `updated` valuations. */
  protected def withValuations(updated: Map[Tradable, Price]): SingleUnitBuyer = {
    new SingleUnitBuyer(issuer, outstandingOrders, updated)
  }

}


/** Companion object for `SingleUnitBuyer`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitBuyer {

  def apply(issuer: Issuer, valuations: Map[Tradable, Price]): SingleUnitBuyer = {
    val outstandingOrders = Map.empty[Token, (Reference, Order[Tradable])]
    new SingleUnitBuyer(issuer, outstandingOrders, valuations)
  }

  def apply(valuations: Map[Tradable, Price]): SingleUnitBuyer = {
    val issuer = UUID.randomUUID()
    val outstandingOrders = Map.empty[Token, (Reference, Order[Tradable])]
    new SingleUnitBuyer(issuer, outstandingOrders, valuations)
  }

}
