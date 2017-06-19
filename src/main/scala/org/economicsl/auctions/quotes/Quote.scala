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

import org.economicsl.core.{Currency, Price}
import play.api.libs.json.{Json, Writes}


/** Base trait for all quote implementations.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait Quote[T] {

  def quote: Option[T]

}


/** Base trait for all price quote implementations.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuote extends Quote[Price] {

  def quote: Option[Price]

}


/** Class implementing an ask price quote.
  *
  * @param quote
  * @author davidrpugh
  * @since 0.1.0
  */
case class AskPriceQuote(quote: Option[Price]) extends PriceQuote


object AskPriceQuote {

  implicit val writes: Writes[AskPriceQuote] = Json.writes[AskPriceQuote]

}

/** Class implementing a bid price quote.
  *
  * @param quote
  * @author davidrpugh
  * @since 0.1.0
  */
case class BidPriceQuote(quote: Option[Price]) extends PriceQuote


object BidPriceQuote {

  implicit val writes: Writes[BidPriceQuote] = Json.writes[BidPriceQuote]

}


/** Class implementing a spread quote.
  *
  * @param quote
  * @author davidrpugh
  * @since 0.1.0
  * @todo a spread is the difference between two prices and as such the unit is not really `Price` but rather should
  *       be `Currency`.
  */
case class SpreadQuote(quote: Option[Currency]) extends Quote[Currency]


object SpreadQuote {

  implicit val writes: Writes[SpreadQuote] = Json.writes[SpreadQuote]

}