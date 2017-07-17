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

import org.economicsl.auctions._
import org.economicsl.auctions.quotes._
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.economicsl.core.{Price, Tradable}

import scala.util.Random


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object ContinuousDoubleAuction extends App {

  val google: TestStock = TestStock()
  val pricingRule = new MidPointPricingPolicy[TestStock]
  val withDiscriminatoryPricing = OpenBidAuction.withDiscriminatoryClearingPolicy(pricingRule)

  // generate a very large stream of random orders...
  type OrderFlow[T <: Tradable] = Stream[(Token, Order[T])]
  val prng = new Random(42)
  val orders: OrderFlow[TestStock] = OrderGenerator.randomOrders(0.5)(1000000, google, prng)

  // A lazy, tail-recursive implementation of a continuous double auction!
  def continuous[T <: Tradable, A <: Auction[T, A]](auction: A)(incoming: OrderFlow[T]): Stream[(A, Option[Stream[SpotContract]])] = {
    @annotation.tailrec
    def loop(da: A, in: OrderFlow[T], out: Stream[(A, Option[Stream[SpotContract]])]): Stream[(A, Option[Stream[SpotContract]])] = in match {
      case Stream.Empty => out
      case head #:: tail =>
        val (updatedAuction, _) = da.insert(head)
        val results @ (residual, _) = updatedAuction.clear
        loop(residual, tail, results #:: out)
    }
    loop(auction, incoming, Stream.empty[(A, Option[Stream[SpotContract]])])
  }

  /** Stream of clear results contains not only the individual filled order streams, but also the residual auction
    * containing the unmatched orders following each clear.  Basically the entire auction history is stored in the
    * stream of clear results.
    */
  val results = continuous[TestStock, OpenBidAuction[TestStock]](withDiscriminatoryPricing)(orders)

  val prices: Stream[Price] = results.flatMap{ case (_, fills) =>
    fills.flatMap(_.headOption).map(_.price)
  }

  val askPriceQuotes: Stream[Quote] = results.map{ case (auction, _) =>
    auction.receive(AskPriceQuoteRequest[TestStock](???))
  }

  val bidPriceQuotes: Stream[Quote] = results.map{ case (auction, _) =>
    auction.receive(BidPriceQuoteRequest[TestStock](???))
  }

  // print off the first 10 prices...
  println(prices.take(10).toList)

  // print off the first 10 spread quotes...
  println(askPriceQuotes.take(100).toList)

  println(bidPriceQuotes.take(100).toList)

}
