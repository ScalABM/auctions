package org.economicsl.auctions.singleunit

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.{Price, Quantity, Tradable}

import scala.collection.GenMap


trait AuctionParticipant
    extends TokenGenerator {

  def issuer: Issuer

  def outstandingOrders: GenMap[Token, (Reference, Order[_ <: Tradable])]

}


object AuctionParticipant {

  final case class Accepted(issuer: Issuer, reference: Reference)

  final case class Canceled(issuer: Issuer, token: Token)

  final case class Executed(issuer: Issuer, token: Token, price: Price, quantity: Quantity, matchNumber: UUID)

  final case class Rejected(issuer: Issuer, reason: String)

}
