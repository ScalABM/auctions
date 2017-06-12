package org.economicsl.auctions.random

import java.util.UUID

import org.economicsl.auctions.{Currency, Price, Quantity, Tradable}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder, Order}


object OrderGenerator {

  def limit(tick: Currency): Random[Price] = {
    RandomGenerator.map(RandomGenerator.positiveMultipleOf(tick))(value => Price(value))
  }

  def quantity(n: Long): Random[Quantity] = {
    RandomGenerator.map(RandomGenerator.positiveLessThan(n))(value => Quantity(value))
  }

  def askOrder[T <: Tradable](issuer: UUID, tradable: T, tick: Currency): Random[LimitAskOrder[T]] = {
    RandomGenerator.map(limit(tick))(price => LimitAskOrder(issuer, price, tradable))
  }

  def bidOrder[T <: Tradable](issuer: UUID, tradable: T, tick: Currency): Random[LimitBidOrder[T]] = {
    RandomGenerator.map(limit(tick))(price => LimitBidOrder(issuer, price, tradable))
  }

  def order[T <: Tradable](askOrderProbability: Double)(issuer: UUID, tradable: T, tick: Currency): Random[Order[T]] = {
    rng =>
      val (sample, rng1) = RandomGenerator.double(rng)
      if (sample <= askOrderProbability) {
        askOrder(issuer, tradable, tick)(rng1)
      } else {
        bidOrder(issuer, tradable, tick)(rng1)
      }
  }

}
