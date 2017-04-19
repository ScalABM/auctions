/*
Copyright 2017 EconomicSL

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
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.MidPointPricingRule
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy

import scala.util.Random


object ContinuousDoubleAuction extends App with OrderGenerator {

  val google: GoogleStock = GoogleStock(tick=1)
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val quotePolicy = new PriceQuotePolicy[GoogleStock]
  val pricingRule = new MidPointPricingRule[GoogleStock]
  val withDiscriminatoryPricing: DoubleAuction[GoogleStock] = DoubleAuction.withOpenOrderBook(orderBook)
                                                                           .withQuotePolicy(quotePolicy)
                                                                           .withDiscriminatoryPricing(pricingRule)
  val prng = new Random(42)
  val orders: Stream[Either[LimitAskOrder[GoogleStock], LimitBidOrder[GoogleStock]]] = {
    randomOrders(10000, google, prng)
  }


  // this shortens type signatures quite a bit...
  type ClearResult = (Option[Stream[Fill[GoogleStock]]], DoubleAuction[GoogleStock])

  // A continuous double auction is a composition of an insert and a clear...can this be made tail recursive???
  def continuous(auction: DoubleAuction[GoogleStock])(incoming: Stream[Either[LimitAskOrder[GoogleStock], LimitBidOrder[GoogleStock]]]): Stream[ClearResult] = {
    incoming match {
      case Stream.Empty => Stream((None, auction))
      case head #:: tail => head match {
        case Left(askOrder) =>
          val (results, residual) = auction.insert(askOrder).clear
          (results, residual) #:: continuous(residual)(tail)
        case Right(bidOrder) =>
          val (results, residual) = auction.insert(bidOrder).clear
          (results, residual) #:: continuous(residual)(tail)
      }
    }
  }

  println(orders)
  // without rationing, the number of fills should match the number of orders
  val results = continuous(withDiscriminatoryPricing)(orders)
  val prices = results.flatMap{ case (fills, _) => fills }.reduce(_ append _).map(fill => fill.price)
  // prices.toList this will generate a stackoverflow! 

}
