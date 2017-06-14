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
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy
import org.economicsl.auctions.singleunit.OrderGenerator
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Random, Success}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
class PeriodicDoubleAuction extends FlatSpec with Matchers with OrderGenerator {

  // generate a stream of random orders...
  val google: GoogleStock = GoogleStock()
  val prng = new Random(42)
  val orders: Stream[Either[AskOrder[GoogleStock], BidOrder[GoogleStock]]] = {
    randomOrders(100, google, prng)
  }

  "A PeriodicDoubleAuction with uniform pricing" should "produce a single price at which all filled orders are processed." in {

    val pricingRule = new MidPointPricingPolicy[GoogleStock]
    val withUniformPricing: SealedBidDoubleAuction.UniformPricingImpl[GoogleStock] = {
      SealedBidDoubleAuction.withUniformPricing(pricingRule, tickSize = 1)
    }

    // this whole process is data parallel...
    val withOrders: SealedBidDoubleAuction.UniformPricingImpl[GoogleStock] = {
      orders.foldLeft(withUniformPricing){
        case (auction, order) => order match {
          case Left(askOrder) => auction.insert(askOrder) match {
            case Success(withAsk) => withAsk
            case _ => auction
          }
          case Right(bidOrder) => auction.insert(bidOrder) match {
            case Success(withBid) => withBid
            case _ => auction
          }
        }
      }
    }

    val results = withOrders.clear
    results.fills.map(_.map(_.price).toSet).size should be(1)

  }

}
