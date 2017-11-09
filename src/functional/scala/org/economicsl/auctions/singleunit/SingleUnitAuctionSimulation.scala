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

import org.economicsl.auctions.messages.OrderId
import org.economicsl.auctions.singleunit.participants.SingleUnitAuctionParticipant
import org.economicsl.auctions.singleunit.pricing.{AskQuotePricingPolicy, BidQuotePricingPolicy}
import org.economicsl.auctions._
import org.economicsl.core.{Currency, Tradable}


trait SingleUnitAuctionSimulation {

  /** Type used to represent a tuple matching an auction participant with its issued order. */
  type IssuedOrder[+T <: Tradable] = (SingleUnitAuctionParticipant, (OrderId, SingleUnitOrder[T]))

  /** Type representing the state of an auction simulation. */
  type State[T <: Tradable, A <: Auction[T, A]] = (A, Iterable[SingleUnitAuctionParticipant])

  /** Issues orders for some auction.
    *
    * @param protocol
    * @param participants
    * @tparam T
    * @return
    */
  def issueOrders[T <: Tradable]
                 (protocol: AuctionProtocol[T], participants: Iterable[SingleUnitAuctionParticipant])
                 : Iterable[IssuedOrder[T]] = {
    participants.flatMap(participant => participant.issueOrder(protocol))
  }

  /** Inserts orders into some auction.
    *
    * @param auction
    * @param issuedOrders
    * @tparam T
    * @tparam A
    * @return
    */
  def insertOrders[T <: Tradable, A <: Auction[T, A]]
                  (auction: A, issuedOrders: Iterable[(SingleUnitAuctionParticipant, (OrderId, SingleUnitOrder[T]))])
                  : (A, Iterable[SingleUnitAuctionParticipant]) = {
    issuedOrders.aggregate((auction, Seq.empty[SingleUnitAuctionParticipant]))(update[T, A], combine[T, A])
  }

  /** Participants issue orders, orders are inserted into the auction and the auction is cleared.
    *
    * @param auction
    * @param participants
    * @tparam T
    * @tparam A
    * @return
    */
  def run[T <: Tradable, A <: Auction[T, A]]
         (auction: A, participants: Iterable[SingleUnitAuctionParticipant])
         : ((A, Iterable[SingleUnitAuctionParticipant]), Option[Iterable[SpotContract]]) = {
    val issuedOrders = issueOrders(auction.protocol, participants)
    val (auctionWithOrders, updatedParticipants) = insertOrders(auction, issuedOrders)
    val (clearedAuction, contracts) = auctionWithOrders.clear
    ((clearedAuction, updatedParticipants), contracts)
  }

  /** Draft type signature for a function that settles contracts. */
  def settle[T <: Tradable, A <: Auction[T, A]]
            (state: State[T, A], contracts: Iterable[Contract])
            : State[T, A] = {
    ???
  }

  /** Creates a first-price open bid auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def firstPriceOpenBidAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): OpenBidAuction[T] = {
    val pricingPolicy = AskQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    OpenBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a first-price open bid auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def firstPriceOpenBidAuction[T <: Tradable](auctionId: AuctionId, tradable: T): OpenBidAuction[T] = {
    firstPriceOpenBidAuction(auctionId, 1L, tradable)
  }

  def firstPriceOpenBidReverseAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): OpenBidAuction[T] = {
    val pricingPolicy = BidQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    OpenBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  def firstPriceOpenBidReverseAuction[T <: Tradable](auctionId: AuctionId, tradable: T): OpenBidAuction[T] = {
    firstPriceOpenBidReverseAuction(auctionId, 1L, tradable)
  }

  /** Creates a first-price sealed bid auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def firstPriceSealedBidAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): SealedBidAuction[T] = {
    val pricingPolicy = AskQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    SealedBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a first-price sealed bid auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def firstPriceSealedBidAuction[T <: Tradable](auctionId: AuctionId, tradable: T): SealedBidAuction[T] = {
    firstPriceSealedBidAuction(auctionId, 1L, tradable)
  }

  def firstPriceSealedBidReverseAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): SealedBidAuction[T] = {
    val pricingPolicy = BidQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    SealedBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  def firstPriceSealedBidReverseAuction[T <: Tradable](auctionId: AuctionId, tradable: T): SealedBidAuction[T] = {
    firstPriceSealedBidReverseAuction(auctionId, 1L, tradable)
  }

  /** Creates a second-price open bid auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceOpenBidAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): OpenBidAuction[T] = {
    val pricingPolicy = BidQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    OpenBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a second-price open bid auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceOpenBidAuction[T <: Tradable](auctionId: AuctionId, tradable: T): OpenBidAuction[T] = {
    secondPriceOpenBidAuction(auctionId, 1L, tradable)
  }

  /** Creates a second-price open bid reverse auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceOpenBidReverseAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): OpenBidAuction[T] = {
    val pricingPolicy = AskQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    OpenBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a second-price open bid reverse auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceOpenBidReverseAuction[T <: Tradable](auctionId: AuctionId, tradable: T): OpenBidAuction[T] = {
    secondPriceOpenBidReverseAuction(auctionId, 1L, tradable)
  }

  /** Creates a second-price sealed bid auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceSealedBidAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): SealedBidAuction[T] = {
    val pricingPolicy = BidQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    SealedBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a second-price sealed bid auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceSealedBidAuction[T <: Tradable](auctionId: AuctionId, tradable: T): SealedBidAuction[T] = {
    secondPriceSealedBidAuction(auctionId, 1L, tradable)
  }

  /** Creates a second-price sealed bid reverse auction for a particular `Tradable`.
    *
    * @param tickSize
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceSealedBidReverseAuction[T <: Tradable](auctionId: AuctionId, tickSize: Currency, tradable: T): SealedBidAuction[T] = {
    val pricingPolicy = AskQuotePricingPolicy[T]()
    val protocol = AuctionProtocol[T](tickSize, tradable)
    SealedBidAuction.withUniformClearingPolicy[T](auctionId, pricingPolicy, protocol)
  }

  /** Creates a second-price sealed bid reverse auction for a particular `Tradable`.
    *
    * @param tradable
    * @tparam T
    * @return
    */
  def secondPriceSealedBidReverseAuction[T <: Tradable](auctionId: AuctionId, tradable: T): SealedBidAuction[T] = {
    secondPriceSealedBidReverseAuction(auctionId, 1L, tradable)
  }

  /** Function that updates the state of auction given an order issued by some auction participant. */
  private[this] def update[T <: Tradable, A <: Auction[T, A]]
                          (state: (A, Seq[SingleUnitAuctionParticipant]), issuedOrder: IssuedOrder[T])
                          : (A, Seq[SingleUnitAuctionParticipant]) = {
    val (auction, participants) = state
    val (participant, order) = issuedOrder
    val (updatedAuction, result) = auction.insert(order)
    val updatedParticipant = participant.handle(result)
    (updatedAuction, updatedParticipant +: participants)
  }


  /** Function that combines two states into a single state. */
  private[this] def combine[T <: Tradable, A <: Auction[T, A]]
                        (state1:(A, Seq[SingleUnitAuctionParticipant]), state2: (A, Seq[SingleUnitAuctionParticipant]))
                        : (A, Seq[SingleUnitAuctionParticipant]) = {
    val (auction, participants) = state1
    val (otherAuction, otherParticipants) = state2
    (auction.combineWith(otherAuction), participants ++ otherParticipants)
  }

}
