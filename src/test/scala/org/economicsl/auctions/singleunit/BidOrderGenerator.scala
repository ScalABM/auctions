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

import org.economicsl.auctions.singleunit.orders.LimitBidOrder
import org.economicsl.core.{Price, Tradable}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
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
