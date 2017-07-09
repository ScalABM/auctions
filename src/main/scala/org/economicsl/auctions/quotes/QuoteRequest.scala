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
package org.economicsl.auctions.quotes

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.core.{Currency, Price, Tradable}


/** Base trait for all quote requests.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait QuoteRequest[T <: Tradable] extends (FourHeapOrderBook[T] => Option[Any])


/** Base trait for all price quote requests.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuoteRequest[T <: Tradable] extends QuoteRequest[T] {
  def apply(orderBook: FourHeapOrderBook[T]): Option[Price]
}


/** Used by auction participants to request the current ask price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final class AskPriceQuoteRequest[T <: Tradable] extends PriceQuoteRequest[T] {
  def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
    orderBook.askPriceQuote
  }
}


/** Used by auction participants to request the current bid price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final class BidPriceQuoteRequest[T <: Tradable] extends PriceQuoteRequest[T] {
  def apply(orderBook: FourHeapOrderBook[T]): Option[Price] = {
    orderBook.bidPriceQuote
  }
}


/** Used by auction participants to request the current spread quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final class SpreadQuoteRequest[T <: Tradable]() extends QuoteRequest[T] {
  def apply(orderBook: FourHeapOrderBook[T]): Option[Currency] = {
    orderBook.spread
  }
}
