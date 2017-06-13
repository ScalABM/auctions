package org.economicsl.auctions.random

import java.util.UUID

import cats.data.State
import org.economicsl.auctions.{Currency, Price, Quantity, Tradable}
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder}


object Random {

  def askOrder[T <: Tradable](issuer: UUID, max: Price, tradable: T, tick: Currency): Random[LimitAskOrder[T]] = {
    limit(tick, max).map(price => LimitAskOrder(issuer, price, tradable))
  }

  def bidOrder[T <: Tradable](issuer: UUID, max: Price, tradable: T, tick: Currency): Random[LimitBidOrder[T]] = {
    limit(tick, max).map(price => LimitBidOrder(issuer, price, tradable))
  }

  private def limit(tick: Currency, max: Price): Random[Price] = {
    positiveMultipleOf(tick).flatMap(value => if (Price(value) < max) State.pure(Price(value)) else limit(tick, max))
  }

  private def quantity(n: Long): Random[Quantity] = {
    positiveLessThan(n).map(value => Quantity(value))
  }

  private def nextLong: Random[Long] = {
    State(seed => (seed.next, seed.current))
  }

  private def nonNegative: Random[Long] = {
    nextLong.map(value => if (value < 0) -(value + 1) else value)
  }

  private def nonNegativeLessThan(n: Long): Random[Long] = {
    nonNegative.flatMap(value => if (value + (n - 1) - (value % n) >= 0) State.pure(value % n) else nonNegativeLessThan(n))
  }

  private def nonNegativeMultipleOf(n: Long): Random[Long] = {
    nonNegative.flatMap(value => if (value % n == 0) State.pure(value) else nonNegativeMultipleOf(n))
  }

  private def positiveLessThan(n: Long): Random[Long] = {
    nonNegativeLessThan(n).flatMap(value => if (value > 0) State.pure(value) else positiveLessThan(n))
  }

  private def positiveMultipleOf(n: Long): Random[Long] = {
    nonNegativeMultipleOf(n).flatMap(value => if (value > 0) State.pure(value) else positiveMultipleOf(n))
  }

}
