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
import org.economicsl.auctions.quotes.{SpreadQuote, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.OrderGenerator
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.economicsl.core.{Price, Tradable}

import scala.util.{Random, Success}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object ContinuousDoubleAuction extends App with OrderGenerator {

  val google: GoogleStock = GoogleStock()
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val pricingRule = new MidPointPricingPolicy[GoogleStock]
  val withDiscriminatoryPricing: OpenBidDoubleAuction.DiscriminatoryPricingImpl[GoogleStock] = {
    OpenBidDoubleAuction.withDiscriminatoryClearingPolicy(pricingRule, tickSize = 1)
  }

  // generate a very large stream of random orders...
  type DoubleAuction[T <: Tradable] = OpenBidDoubleAuction.DiscriminatoryPricingImpl[T]
  type OrderFlow[T <: Tradable] = Stream[Either[AskOrder[T], BidOrder[T]]]
  val prng = new Random(42)
  val orders: Stream[Either[AskOrder[GoogleStock], BidOrder[GoogleStock]]] = randomOrders(1000000, google, prng)

  // A lazy, tail-recursive implementation of a continuous double auction!
  def continuous[T <: Tradable](auction: DoubleAuction[T])(incoming: OrderFlow[T]): Stream[ClearResult[DoubleAuction[T]]] = {
    @annotation.tailrec
    def loop(da: DoubleAuction[T], in: OrderFlow[T], out: Stream[ClearResult[DoubleAuction[T]]]): Stream[ClearResult[DoubleAuction[T]]] = in match {
      case Stream.Empty => out
      case head #:: tail => head match {
        case Left(askOrder) =>
          da.insert(askOrder) match {
            case Success(withAsk) =>
              val results = withAsk.clear
              loop(results.residual, tail, results #:: out)
            case _ =>
              loop(da, tail, out)
          }
        case Right(bidOrder) =>
          da.insert(bidOrder) match {
            case Success(withBid) =>
              val results = withBid.clear
              loop(results.residual, tail, results #:: out)
            case _ =>
              loop(da, tail, out)
          }
      }
    }
    loop(auction, incoming, Stream.empty[ClearResult[DoubleAuction[T]]])
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
