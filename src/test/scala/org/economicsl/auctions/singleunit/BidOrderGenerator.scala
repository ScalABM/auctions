package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Price, Tradable}

import scala.util.Random



trait BidOrderGenerator {

  def randomBidOrder[T <: Tradable](tradable: T, prng: Random): LimitBidOrder[T] = {
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val limit = Price(prng.nextInt(Int.MaxValue))
    LimitBidOrder(issuer, limit, tradable)
  }

  def randomBidOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[LimitBidOrder[T]] = {
    @annotation.tailrec
    def loop(accumulated: Stream[LimitBidOrder[T]], remaining: Int): Stream[LimitBidOrder[T]] = {
      if (remaining == 0) {
        accumulated
      } else {
        val bid = randomBidOrder(tradable, prng)
        loop(bid #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[LimitBidOrder[T]], n)
  }

}
