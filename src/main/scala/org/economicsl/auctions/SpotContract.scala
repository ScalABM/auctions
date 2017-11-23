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

import org.economicsl.auctions.messages.{NewSinglePricePointBid, NewSinglePricePointOffer, NewSingleUnitBid, NewSingleUnitOffer}
import org.economicsl.core.{Price, Quantity, Tradable}
import play.api.libs.json.{Json, Writes}

/**
  *
  * @param issuer
  * @param counterparty
  * @param price
  * @param quantity
  * @param tradable
  * @author davidrpugh
  * @since 0.1.0
  */
case class SpotContract(issuer: Buyer, counterparty: Seller, price: Price, quantity: Quantity, tradable: Tradable)
  extends Contract


object SpotContract {

  implicit val writes: Writes[SpotContract] = Json.writes[SpotContract]

  /** Creates a `SpotContract` from a matched `NewSinglePricePointBid` and `NewSinglePricePointOffer`.
    *
    * @param bid
    * @param offer
    * @param price
    * @tparam T
    * @return
    */
  def fromSinglePricePointOrders[T <: Tradable]
                                (bid: NewSinglePricePointBid[T], offer: NewSinglePricePointOffer[T], price: Price)
                                : SpotContract = {
    require(offer.limit <= price); require(price <= bid.limit)  // incentive compatibility check!
    SpotContract(bid.senderId, offer.senderId, price, offer.quantity min bid.quantity, offer.tradable)
  }

  /** Creates a `SpotContract` from a matched `NewSingleUnitBid` and `NewSingleUnitOffer`.
    *
    * @param bid
    * @param offer
    * @param price
    * @tparam T
    * @return
    */
  def fromSingleUnitOrders[T <: Tradable]
                          (bid: NewSingleUnitBid[T], offer: NewSingleUnitOffer[T], price: Price)
                          : SpotContract = {
    require(offer.limit <= price); require(price <= bid.limit)  // incentive compatibility check!
    SpotContract(bid.senderId, offer.senderId, price, offer.quantity min bid.quantity, offer.tradable)
  }
}
