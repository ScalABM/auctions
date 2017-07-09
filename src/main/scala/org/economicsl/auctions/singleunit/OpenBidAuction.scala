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

import org.economicsl.auctions.quotes.{Quote, QuoteRequest}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.core.{Currency, Tradable}


/** Base trait for representing an "open-bid" double auction mechanism.
  *
  * @tparam T all `AskOrder` and `BidOrder` instances submitted to the `OpenBidAuction` must be for the same
  *           type of `Tradable`.
  * @author davidrpugh
  * @since 0.1.0
  */
abstract class OpenBidAuction[T <: Tradable, A <: OpenBidAuction[T, A]](
  orderBook: FourHeapOrderBook[T],
  pricingPolicy: PricingPolicy[T],
  tickSize: Currency)
    extends Auction[T, A](orderBook, pricingPolicy, tickSize) {
  this: A =>

  def receive(request: QuoteRequest[T]): Quote

}