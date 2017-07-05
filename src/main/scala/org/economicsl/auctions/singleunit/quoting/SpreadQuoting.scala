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
package org.economicsl.auctions.singleunit.quoting

import org.economicsl.auctions.quotes.{SpreadQuote, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.core.Tradable


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SpreadQuoting[T <: Tradable] {
  this: Auction[T] =>

  def receive(request: SpreadQuoteRequest[T]): SpreadQuote = {
    spreadQuotingPolicy(orderBook, request)
  }

  protected val spreadQuotingPolicy: SpreadQuotingPolicy[T] = new SpreadQuotingPolicy[T]

}
