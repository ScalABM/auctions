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

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orders.SingleUnitBidOrder
import org.economicsl.core.{Price, Tradable}

import scala.collection.immutable


/** A `TestSingleUnitBidOrderIssuer` has fixed valuations and always bids as close to its valuation as possible when
  * issuing `SingleUnitBidOrders`.
  *
  * @param issuer
  * @param outstandingOrders
  * @param valuations
  */
class TestSingleUnitBidOrderIssuer private(
  val issuer: Issuer,
  val outstandingOrders: Map[Token, (Reference, Order[Tradable])],
  protected val valuations: Map[Tradable, Price])
    extends AuctionParticipant[TestSingleUnitBidOrderIssuer] {

  /** Each `TestSingleUnitBidOrderIssuer` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return a `Tuple2` whose first element contains a `Token` that uniquely identifies an `Order` and whose second
    *         element is an `Order`.
    * @note care is needed in order to guarantee that the limit price is a multiple of the tick size.
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): (TestSingleUnitBidOrderIssuer, (Token, SingleUnitBidOrder[T])) = {
    // if valuation is not multiple of tick size, price is largest multiple of tick size less than valuation.
    val valuation = valuations.getOrElse(protocol.tradable, Price.MaxValue)
    val remainder = valuation.value % protocol.tickSize
    val limit = if (valuation.isMultipleOf(protocol.tickSize)) valuation else Price(valuation.value - remainder)
    (this, (randomToken(), SingleUnitBidOrder(issuer, limit, protocol.tradable)))
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Order[Tradable])]): TestSingleUnitBidOrderIssuer = {
    new TestSingleUnitBidOrderIssuer(issuer, updated, valuations)
  }

}


object TestSingleUnitBidOrderIssuer {

  def withNoOutstandingOrders(issuer: Issuer, valuations: Map[Tradable, Price]): TestSingleUnitBidOrderIssuer = {
    val emptyOutstandingOrders = immutable.HashMap.empty[Token, (Reference, Order[Tradable])]
    new TestSingleUnitBidOrderIssuer(issuer, emptyOutstandingOrders, valuations)
  }

}
