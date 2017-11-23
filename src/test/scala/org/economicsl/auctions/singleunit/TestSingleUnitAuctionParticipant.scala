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
import org.economicsl.auctions.messages._
import org.economicsl.auctions.singleunit.participants.SingleUnitAuctionParticipant
import org.economicsl.core.{Price, Tradable}

import scala.collection.immutable
import scala.util.Random


/** A `TestOrderIssuer` has fixed valuations and always bids (or offers) as close to its valuation as possible when
  * issuing orders.
  *
  * @param prng a pseudo-random number generator.
  * @param askOrderProbability probability that the `TestOrderIssuer` generates a `SingleLimitAskOrder`.
  * @param participantId
  * @param issuedOrders
  * @param outstandingOrders
  * @param valuations
  */
class TestSingleUnitAuctionParticipant private(
  prng: Random,
  askOrderProbability: Double,
  val participantId: IssuerId,
  val issuedOrders: Map[OrderId, NewOrder[Tradable]],
  val outstandingOrders: Map[OrderId, (OrderReferenceId, NewOrder[Tradable])],
  val valuations: Map[Tradable, Price])
    extends SingleUnitAuctionParticipant {

  /** Returns a new `AuctionParticipant` that has observed the `AuctionDataResponse`.
    *
    * @param auctionDataResponse
    * @return
    */
  def handle[T <: Tradable](auctionDataResponse: AuctionDataResponse[T]): TestSingleUnitAuctionParticipant = {
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
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(TestSingleUnitAuctionParticipant, NewSingleUnitOrder[T])] = {
    if (prng.nextDouble() <= askOrderProbability) {
      // if valuation is not multiple of tick size, price is smallest multiple of tick size greater than valuation.
      val valuation = valuations.getOrElse(protocol.tradable, Price.MinValue)
      val remainder = valuation.value % protocol.tickSize
      val limit = if (valuation.isMultipleOf(protocol.tickSize)) valuation else Price(valuation.value + (protocol.tickSize - remainder))
      val orderId = randomOrderId()
      val timestamp = currentTimeMillis()
      val newOffer = SingleUnitOffer(limit, orderId, participantId ,timestamp, protocol.tradable)
      val updated = issuedOrders + (orderId -> newOffer)
      Some((withIssuedOrders(updated), newOffer))
    } else {
      // if valuation is not multiple of tick size, price is largest multiple of tick size less than valuation.
      val valuation = valuations.getOrElse(protocol.tradable, Price.MaxValue)
      val remainder = valuation.value % protocol.tickSize
      val orderId = randomOrderId()
      val limit = if (valuation.isMultipleOf(protocol.tickSize)) valuation else Price(valuation.value - remainder)
      val timestamp = currentTimeMillis()
      val newBid = SingleUnitBid(limit, orderId, participantId, timestamp, protocol.tradable)
      val updated = issuedOrders + (orderId -> newBid)
      Some((withIssuedOrders(updated), newBid))
    }
  }

  /** Each `AuctionParticipant` needs to request auction data given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def requestAuctionData[T <: Tradable](protocol: AuctionProtocol[T]): Option[(TestSingleUnitAuctionParticipant, (OrderId, AuctionDataRequest[T]))] = {
    None
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withIssuedOrders(updated: Map[OrderId, NewOrder[Tradable]]): TestSingleUnitAuctionParticipant = {
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, participantId, updated, outstandingOrders, valuations)
  }

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[OrderId, (OrderReferenceId, NewOrder[Tradable])]): TestSingleUnitAuctionParticipant = {
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, participantId, issuedOrders, updated, valuations)
  }

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withValuations(updated: Map[Tradable, Price]): TestSingleUnitAuctionParticipant = {
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, participantId, issuedOrders, outstandingOrders, updated)
  }

}


object TestSingleUnitAuctionParticipant {

  def withNoOutstandingOrders(prng: Random,
                              askOrderProbability: Double,
                              issuer: IssuerId,
                              valuations: Map[Tradable, Price])
                             : TestSingleUnitAuctionParticipant = {
    val emptyIssuedOrders = immutable.HashMap.empty[OrderId, NewOrder[Tradable]]
    val emptyOutstandingOrders = immutable.HashMap.empty[OrderId, (OrderReferenceId, NewOrder[Tradable])]
    new TestSingleUnitAuctionParticipant(prng, askOrderProbability, issuer, emptyIssuedOrders, emptyOutstandingOrders, valuations)
  }

}
