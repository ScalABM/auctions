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
import org.economicsl.auctions.messages.{AuctionDataRequest, AuctionDataResponse}
import org.economicsl.auctions.singleunit.orders.{SingleUnitAskOrder, SingleUnitBidOrder, SingleUnitOrder}
import org.economicsl.auctions.singleunit.participants.SingleUnitAuctionParticipant
import org.economicsl.core.{Price, Tradable}

import scala.collection.immutable
import scala.util.Random


/** A `TestOrderIssuer` has fixed valuations and always bids (or offers) as close to its valuation as possible when
  * issuing orders.
  *
  * @param prng a pseudo-random number generator.
  * @param askOrderProbability probability that the `TestOrderIssuer` generates a `SingleLimitAskOrder`.
  * @param issuer
  * @param outstandingOrders
  * @param valuations
  */
class TestSingleUnitAuctionParticipant private(
  prng: Random,
  askOrderProbability: Double,
  val issuer: Issuer,
  val outstandingOrders: Map[Token, (Reference, Order[Tradable])],
  val valuations: Map[Tradable, Price])
    extends SingleUnitAuctionParticipant {


  /** Returns a new `AuctionParticipant` that has observed the `AuctionDataResponse`.
    *
    * @param auctionDataResponse
    * @return
    */
  def handle(auctionDataResponse: AuctionDataResponse): SingleUnitAuctionParticipant = {
    ???
  }

  /** Each `OrderIssuer` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return a `Tuple2` whose first element contains a `Token` that uniquely identifies an `Order` and whose second
    *         element is an `Order`.
    * @note care is needed in order to guarantee that the limit price is a multiple of the tick size.
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(TestSingleUnitAuctionParticipant, (Token, SingleUnitOrder[T]))] = {
    if (prng.nextDouble() <= askOrderProbability) {
      // if valuation is not multiple of tick size, price is smallest multiple of tick size greater than valuation.
      val valuation = valuations.getOrElse(protocol.tradable, Price.MinValue)
      val remainder = valuation.value % protocol.tickSize
      val limit = if (valuation.isMultipleOf(protocol.tickSize)) valuation else Price(valuation.value + (protocol.tickSize - remainder))
      Some((this, (randomToken(), SingleUnitAskOrder(issuer, limit, protocol.tradable))))
    } else {
      // if valuation is not multiple of tick size, price is largest multiple of tick size less than valuation.
      val valuation = valuations.getOrElse(protocol.tradable, Price.MaxValue)
      val remainder = valuation.value % protocol.tickSize
      val limit = if (valuation.isMultipleOf(protocol.tickSize)) valuation else Price(valuation.value - remainder)
      Some((this, (randomToken(), SingleUnitBidOrder(issuer, limit, protocol.tradable))))
    }
  }


  /** Each `AuctionParticipant` needs to request auction data given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def requestAuctionData[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitAuctionParticipant, (Token, AuctionDataRequest[T]))] = {
    None
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Order[Tradable])]): TestSingleUnitAuctionParticipant = {
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, issuer, updated, valuations)
  }

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withValuations(updated: Map[Tradable, Price]): TestSingleUnitAuctionParticipant = {
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, issuer, outstandingOrders, updated)
  }

}


object TestSingleUnitAuctionParticipant {

  def withNoOutstandingOrders(prng: Random,
                              askOrderProbability: Double,
                              issuer: Issuer,
                              valuations: Map[Tradable, Price])
                             : TestSingleUnitAuctionParticipant = {
    val emptyOutstandingOrders = immutable.HashMap.empty[Token, (Reference, Order[Tradable])]
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, issuer, emptyOutstandingOrders, valuations)
  }

}
