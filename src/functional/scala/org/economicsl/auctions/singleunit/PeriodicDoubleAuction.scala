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


object PeriodicDoubleAuction extends App with AskOrderGenerator with BidOrderGenerator {

  val google: GoogleStock = GoogleStock(tick=1)
  val orderBook = FourHeapOrderBook.empty[GoogleStock]
  val quotePolicy = new PriceQuotePolicy[GoogleStock]
  val pricingRule = new MidPointPricingRule[GoogleStock]
  val withUniformPricing: DoubleAuction[GoogleStock] = DoubleAuction.withOpenOrderBook(orderBook)
                                                                    .withQuotePolicy(quotePolicy)
                                                                    .withUniformPricing(pricingRule)


  val prng = new Random(42)
  val offers = randomAskOrders(100, google, prng)
  val bids = randomBidOrders(100, google, prng)

  // insert all of the orders...this can be done concurrently using Akka or (if no removing orders) in parallel)
  val withBids = bids.foldLeft(withUniformPricing)((auction, bidOrder) => auction.insert(bidOrder))
  val withOrders = offers.foldLeft(withBids)((auction, askOrder) => auction.insert(askOrder))

  // without rationing, the number of fills should match the number of orders
  val (results, _) = withOrders.clear

  val unitsSold: Option[Quantity] = results.map(fills => fills.foldLeft(Quantity(0))((total, fill) => total + fill.quantity))
  println(results.map(fills => fills.map(fill => fill.price).toSet))  // should contain only one value!
  println(unitsSold)

}
