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
package org.economicsl.auctions.singleunit.participants

import org.economicsl.auctions.messages.NewSingleUnitOrder
import org.economicsl.auctions.{AuctionParticipant, AuctionProtocol}
import org.economicsl.core.Tradable


/** Base trait for all `SingleUnitAuctionParticipant` implementations.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait SingleUnitAuctionParticipant
  extends AuctionParticipant[SingleUnitAuctionParticipant] {

  /** Each `AuctionParticipant` needs to issue orders given some `AuctionProtocol`.
    *
    * @param protocol
    * @tparam T
    * @return
    */
  def issueOrder[T <: Tradable](protocol: AuctionProtocol[T]): Option[(SingleUnitAuctionParticipant, NewSingleUnitOrder[T])]

}
