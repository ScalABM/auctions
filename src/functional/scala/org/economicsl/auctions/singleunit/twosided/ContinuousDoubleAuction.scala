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
package org.economicsl.auctions.singleunit.twosided

import org.economicsl.auctions._
import org.economicsl.auctions.quotes.{PriceQuote, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.economicsl.auctions.singleunit.quoting.{PriceQuotePolicy, PriceQuoting}
import org.economicsl.auctions.singleunit.{Order, OrderGenerator}

import scala.util.Random


object ContinuousDoubleAuction extends App with OrderGenerator {

  val google: GoogleStock = GoogleStock(tick=1)
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val quotePolicy = new PriceQuotePolicy[GoogleStock]
  val pricingRule = new MidPointPricingPolicy[GoogleStock]
  val withDiscriminatoryPricing: DoubleAuction[GoogleStock] with PriceQuoting = {
    DoubleAuction.withOpenOrderBook(orderBook)
      .withQuotePolicy(quotePolicy)
      .withDiscriminatoryPricing(pricingRule)
  }

  // generate a very large stream of random orders...
  val prng = new Random(42)
  val orders: Stream[Order[GoogleStock]] = randomOrders(1000000, google, prng)

  // A lazy, tail-recursive implementation of a continuous double auction!
  def continuous[T <: Tradable, A <: DoubleAuctionLike[T, A]](auction: A)(incoming: Stream[Order[T]]): Stream[ClearResult[T, A]] = {
    @annotation.tailrec
    def loop(da: A, in: Stream[Order[T]], out: Stream[ClearResult[T, A]]): Stream[ClearResult[T, A]] = in match {
      case Stream.Empty => out
      case head #:: tail =>
        val results = da.insert(head).clear
        loop(results.residual, tail, results #:: out)
    }
    loop(auction, incoming, Stream.empty[ClearResult[T, A]])
  }

  /** Stream of clear results contains not only the individual filled order streams, but also the residual auction
    * containing the unmatched orders following each clear.  Basically the entire auction history is stored in the
    * stream of clear results.
    */
  val results = continuous(withDiscriminatoryPricing)(orders)

  val prices: Stream[Price] = results.flatMap(result => result.fills)
                                     .flatMap(fills => fills.headOption)
                                     .map(fill => fill.price)
  // print off the first 10 prices...
  println(prices.take(10).toList)

  val spreads: Stream[PriceQuote] = results.map(result => result.residual)
                                           .flatMap(auction => auction.receive(new SpreadQuoteRequest))

  // print off the first 10 prices...
  println(spreads.take(10).toList)

}
