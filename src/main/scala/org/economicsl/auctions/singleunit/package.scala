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
package org.economicsl.auctions


/** Following Wurman et al (1998) we characterize auctions in terms of how each auction mechanism manages the following
  * three core activities.
  *
  * - '''Process `AskOrder` and/or `BidOrder` instances:''' An auction mechanism needs to check the validity of each received
  * order and then update its collection of active orders accordingly.
  * - '''Clearing:''' Primary function of any auction mechanism is to transform orders (i.e., contracts between a particular
  * buyer (seller) and an arbitrary seller (buyer)) into fills (i.e., contracts between a particular buyer (seller) and
  * a particular seller (buyer)). The clearing process should match buyers and sellers as well as set the fill price.
  * - '''Generate price quotes:''' Some auction types will reveal information about the current state of the auction
  * during the bidding process. Typically, the state of an auction will be summarized by a ''price quote''. A
  * `PriceQuote` is defined as the price that an auction participant would have had to offer in order for its
  * `BidOrder` to have been accepted had the auction cleared at the time when the `QuoteRequest` was received.
  *
  */
package object singleunit
