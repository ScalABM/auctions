package org.economicsl.auctions

import org.economicsl.core.Tradable

import scala.collection.GenMap


trait AuctionParticipant
    extends TokenGenerator {

  def issuer: Issuer

  def outstandingContracts: GenMap[Token, (Reference, Contract with OrderLike[_ <: Tradable])]

}


object AuctionParticipant {

  final case class Accepted(issuer: Issuer, reference: Reference)

  final case class Canceled(issuer: Issuer, token: Token)

  final case class Rejected(issuer: Issuer, reason: String)

}
