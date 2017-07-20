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

import org.economicsl.auctions.Issuer
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.core.Tradable


/** Base trait for all quote requests.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait QuoteRequest[T <: Tradable] {

  /* Unique identifier for the auction participant that issued the `QuoteRequest`. */
  def issuer: Issuer

  def query: FourHeapOrderBook[T] => Quote

}

/** Base trait for all price quote requests.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuoteRequest[T <: Tradable] extends QuoteRequest[T] {

  def query: (FourHeapOrderBook[T]) => PriceQuote

}


/** Used by auction participants to request the current ask price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class AskPriceQuoteRequest[T <: Tradable](issuer: Issuer) extends PriceQuoteRequest[T] {

  val query: (FourHeapOrderBook[T]) => AskPriceQuote = {
    ob => AskPriceQuote(issuer, ob.askPriceQuote)
  }

}


/** Used by auction participants to request the current bid price quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class BidPriceQuoteRequest[T <: Tradable](issuer: Issuer) extends PriceQuoteRequest[T] {

  val query: FourHeapOrderBook[T] => BidPriceQuote = {
    ob => BidPriceQuote(issuer, ob.bidPriceQuote)
  }

}


/** Used by auction participants to request the current spread quote.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
final case class SpreadQuoteRequest[T <: Tradable](issuer: Issuer) extends QuoteRequest[T] {

  val query: FourHeapOrderBook[T] => SpreadQuote = {
    ob => SpreadQuote(issuer, ob.spread)
  }

}
