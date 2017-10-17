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
package org.economicsl.auctions.messages


import org.economicsl.core.{Price, Tradable}
import play.api.libs.json.{Json, Writes}


/** Base trait for all price quote implementations.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuote[+T <: Tradable] extends AuctionData[T] {

  def value: Option[Price]

}


/** Class implementing an ask price quote.
  *
  * @param tradable
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class AskPriceQuote[+T <: Tradable](tradable: T, value: Option[Price]) extends PriceQuote[T]


object AskPriceQuote {

  implicit def writes[T <: Tradable]: Writes[AskPriceQuote[T]] = Json.writes[AskPriceQuote[T]]

}


/** Class implementing a bid price quote.
  *
  * @param tradable
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class BidPriceQuote[+T <: Tradable](tradable: T, value: Option[Price]) extends PriceQuote[T]


object BidPriceQuote {

  implicit def writes[T <: Tradable]: Writes[BidPriceQuote[T]] = Json.writes[BidPriceQuote[T]]

}


/** Class implementing a mid-point price quote.
  *
  * @param tradable
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class MidPointPriceQuote[+T <: Tradable](tradable: T, value: Option[Price]) extends PriceQuote[T]


object MidPointPriceQuote {

  implicit def writes[T <: Tradable]: Writes[MidPointPriceQuote[T]] = Json.writes[MidPointPriceQuote[T]]

}