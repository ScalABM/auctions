package org.economicsl.auctions.singleunit

import java.util.UUID

import org.economicsl.auctions.{Price, Tradable}

import scala.util.Random


trait OrderGenerator extends AskOrderGenerator with BidOrderGenerator {

  def randomOrder[T <: Tradable](tradable: T, prng: Random): Either[LimitAskOrder[T], LimitBidOrder[T]] = {
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val limit = Price(prng.nextInt(Int.MaxValue))
    if (prng.nextDouble() <= 0.5) {
      Left(LimitAskOrder(issuer, limit, tradable))
    } else {
      Right(LimitBidOrder(issuer, limit, tradable))
    }
  }


  def randomOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]] = {
    @annotation.tailrec
    def loop(accumulated: Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]], remaining: Int): Stream[Either[LimitAskOrder[T], LimitBidOrder[T]]] = {
      if (remaining == 0) {
        accumulated
      } else {
        val order = randomOrder(tradable, prng)
        loop(order #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[Either[LimitAskOrder[T], LimitBidOrder[T]]], n)
  }

}
