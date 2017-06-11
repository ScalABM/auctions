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

import org.economicsl.auctions.singleunit.orders.LimitAskOrder
import org.economicsl.auctions.{Price, Tradable}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
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
