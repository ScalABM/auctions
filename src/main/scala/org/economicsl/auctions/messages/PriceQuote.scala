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


import org.economicsl.core.Price
import play.api.libs.json.{Json, Writes}


/** Base trait for all price quote implementations.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait PriceQuote extends AuctionData {

  def value: Option[Price]

}


/** Class implementing an ask price quote.
  *
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class AskPriceQuote(value: Option[Price]) extends PriceQuote


object AskPriceQuote {

  implicit val writes: Writes[AskPriceQuote] = Json.writes[AskPriceQuote]

}


/** Class implementing a bid price quote.
  *
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class BidPriceQuote(value: Option[Price]) extends PriceQuote


object BidPriceQuote {

  implicit val writes: Writes[BidPriceQuote] = Json.writes[BidPriceQuote]

}


/** Class implementing a mid-point price quote.
  *
  * @param value
  * @author davidrpugh
  * @since 0.1.0
  */
case class MidPointPriceQuote(value: Option[Price]) extends PriceQuote


object MidPointPriceQuote {

  implicit val writes: Writes[MidPointPriceQuote] = Json.writes[MidPointPriceQuote]

}