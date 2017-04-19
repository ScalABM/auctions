package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Price, Tradable}

import scala.util.Random



trait AskOrderGenerator {

  def randomAskOrder[T <: Tradable](tradable: T, prng: Random): LimitAskOrder[T] = {
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val limit = Price(prng.nextInt(Int.MaxValue))
    LimitAskOrder(issuer, limit, tradable)
  }


  def randomAskOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[LimitAskOrder[T]] = {
    @annotation.tailrec
    def loop(accumulated: Stream[LimitAskOrder[T]], remaining: Int): Stream[LimitAskOrder[T]] = {
      if (remaining == 0) {
        accumulated
      } else {
        val ask = randomAskOrder(tradable, prng)
        loop(ask #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[LimitAskOrder[T]], n)
  }

}
