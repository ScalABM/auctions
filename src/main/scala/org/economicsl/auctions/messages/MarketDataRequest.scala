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

import java.util.UUID


/** Base trait for all `MarketDataRequest` types.
  *
  * @tparam A should be a subtype of `Auction` so that auction designer can restrict the type of market data requests
  *           that an `AuctionParticipant` can issue.
  */
sealed trait MarketDataRequest[-A] {

  /** Function that returns some `MarketData` as a function of `Auction` state. */
  def query: A => Option[MarketData]

}









