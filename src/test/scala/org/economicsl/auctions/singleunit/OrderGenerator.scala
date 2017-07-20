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

import java.util.UUID

import org.economicsl.auctions.{Token, TokenGenerator}
import org.economicsl.auctions.singleunit.orders._
import org.economicsl.core.{Price, Tradable}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object OrderGenerator extends TokenGenerator {

  def randomAskOrder[T <: Tradable](tradable: T, prng: Random): (Token, LimitAskOrder[T]) = {
    val token = randomToken()
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val limit = randomPrice(minimum=Price.MinValue, maximum=Price.MaxValue, prng)
    (token, LimitAskOrder(issuer, limit, tradable))
  }


  def randomAskOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[(Token, LimitAskOrder[T])] = {
    @annotation.tailrec
    def loop(accumulated: Stream[(Token, LimitAskOrder[T])], remaining: Int): Stream[(Token, LimitAskOrder[T])] = {
      if (remaining == 0) {
        accumulated
      } else {
        val ask = randomAskOrder(tradable, prng)
        loop(ask #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[(Token, LimitAskOrder[T])], n)
  }

  def randomBidOrder[T <: Tradable](tradable: T, prng: Random): (Token, LimitBidOrder[T]) = {
    val issuer = UUID.randomUUID()  // todo make this reproducible!
    val token = randomToken()
    val limit = randomPrice(minimum=Price.MinValue, maximum=Price.MaxValue, prng)
    (token, LimitBidOrder(issuer, limit, tradable))
  }

  def randomBidOrders[T <: Tradable](n: Int, tradable: T, prng: Random): Stream[(Token, LimitBidOrder[T])] = {
    @annotation.tailrec
    def loop(accumulated: Stream[(Token, LimitBidOrder[T])], remaining: Int): Stream[(Token, LimitBidOrder[T])] = {
      if (remaining == 0) {
        accumulated
      } else {
        val bid = randomBidOrder(tradable, prng)
        loop(bid #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[(Token, LimitBidOrder[T])], n)
  }


  def randomOrder[T <: Tradable](askOrderProbability: Double)(tradable: T, prng: Random): (Token, Order[T]) = {
    if (prng.nextDouble() <= askOrderProbability) {
      randomAskOrder(tradable, prng)
    } else {
      randomBidOrder(tradable, prng)
    }
  }


  def randomOrders[T <: Tradable](askOrderProbability: Double)(n: Int, tradable: T, prng: Random): Stream[(Token, Order[T])] = {
    @annotation.tailrec
    def loop(accumulated: Stream[(Token, Order[T])], remaining: Int): Stream[(Token, Order[T])] = {
      if (remaining == 0) {
        accumulated
      } else {
        val order = randomOrder(askOrderProbability)(tradable, prng)
        loop(order #:: accumulated, remaining - 1)
      }
    }
    loop(Stream.empty[(Token, Order[T])], n)
  }


  def randomPrice(minimum: Price, maximum: Price, prng: Random): Price = {
    Price(minimum.value + math.abs(prng.nextLong()) % (maximum.value - minimum.value))
  }

}
