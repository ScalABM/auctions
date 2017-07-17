package org.economicsl.auctions.actors

import akka.actor.Props
import org.economicsl.auctions.singleunit.SealedBidAuction
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


class SealedBidAuctionActor[T <: Tradable] private(protected var auction: SealedBidAuction[T])
    extends AuctionActor[T, SealedBidAuction[T]] {

}


object SealedBidAuctionActor {

  def withDiscriminatoryClearingPolicy[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): Props = {
    val auction = SealedBidAuction.withDiscriminatoryClearingPolicy(pricingPolicy, tickSize)
    Props(new SealedBidAuctionActor(auction))
  }

  def withUniformClearingPolicy[T <: Tradable](pricingPolicy: PricingPolicy[T], tickSize: Currency): Props = {
    val auction = SealedBidAuction.withUniformClearingPolicy(pricingPolicy, tickSize)
    Props(new SealedBidAuctionActor(auction))
  }

}
