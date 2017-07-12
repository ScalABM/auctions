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
import org.economicsl.auctions.quotes.{SpreadQuote, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
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

  val google: GoogleStock = GoogleStock()
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val pricingRule = new MidPointPricingPolicy[GoogleStock]
  val withDiscriminatoryPricing = OpenBidAuction.withDiscriminatoryClearingPolicy(pricingRule, tickSize = 1)

  // generate a very large stream of random orders...
  type OrderFlow[T <: Tradable] = Stream[(Token, Order[T])]
  val prng = new Random(42)
  val orders: Stream[(Token, Order[GoogleStock])] = OrderGenerator.randomOrders(0.5)(1000000, google, prng)

  // A lazy, tail-recursive implementation of a continuous double auction!
  def continuous[T <: Tradable](auction: SealedBidAuction[T])(incoming: OrderFlow[T]): Stream[(SealedBidAuction[T], Option[Stream[Fill]])] = {
    @annotation.tailrec
    def loop(da: SealedBidAuction[T], in: OrderFlow[T], out: Stream[(SealedBidAuction[T], Option[Stream[Fill]])]): Stream[(SealedBidAuction[T], Option[Stream[Fill]])] = in match {
      case Stream.Empty => out
      case head #:: tail =>
        val (withAsk, _) = da.insert(head)
        val results @ (residual, _) = withAsk.clear
        loop(residual, tail, results #:: out)
    }
    loop(auction, incoming, Stream.empty[(SealedBidAuction[T], Option[Stream[Fill]])])
  }

  /** Stream of clear results contains not only the individual filled order streams, but also the residual auction
    * containing the unmatched orders following each clear.  Basically the entire auction history is stored in the
    * stream of clear results.
    */
  val results = continuous[GoogleStock](withDiscriminatoryPricing)(orders)

  val prices: Stream[Price] = {
    results.flatMap(result => result.fills)
      .flatMap(fills => fills.headOption)
      .map(fill => fill.price)
  }

  val spreadQuotes: Stream[SpreadQuote] = {
    results.map(result => result.residual.receive(SpreadQuoteRequest[GoogleStock]()))
  }

  // print off the first 10 prices...
  println(prices.take(10).toList)

  // print off the first 10 spread quotes...
  println(spreadQuotes.take(100).toList)

}
