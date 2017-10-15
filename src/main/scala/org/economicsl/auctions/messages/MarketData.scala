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


/** Base trait for all `MarketData` messages.
  *
  * A `MarketData` message may be sent in response to a `MarketDataRequest` message or may be unsolicited.
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait MarketData {

  def mDReqId: Option[String]

  def msgType: Char

}


/** Base trait for all `MarketDataSnapshot` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  * @note See the relevant [[https://www.onixs.biz/fix-dictionary/5.0/msgType_W_87.html Fix protocol docs]] for details.
  */
trait MarketDataSnapshot extends MarketData {
  val msgType: Char = 'W'
}


/** Base trait for all `MarketDataFullRefresh` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  * @note See the relevant [[https://www.onixs.biz/fix-dictionary/5.0/msgType_W_87.html Fix protocol docs]] for details.
  */
trait MarketDataFullRefresh extends MarketData {
  val msgType: Char = 'W'
}


/** Base trait for all `MarketDataIncrementalRefresh` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  * @note See the relevant [[https://www.onixs.biz/fix-dictionary/5.0/msgType_X_88.html Fix protocol docs]] for details.
  */
trait MarketDataIncrementalRefresh extends MarketData {
  val msgType: Char = 'X'
}
