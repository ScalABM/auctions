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

import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Base trait for all `AuctionDataResponse` messages.
  *
  * An `AuctionDataResponse` message may be sent in response to a `AuctionDataRequest` message or may be unsolicited.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
final case class AuctionDataResponse[+T <: Tradable](
  data: AuctionData[T],
  senderId: SenderId,
  mDReqId: Option[UUID],
  timestamp: Timestamp)
    extends Message


/** Companion object for the `AuctionDataResponse` class.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object AuctionDataResponse {

  /** Create an `AuctionDataResponse` for a previously submitted `AuctionDataRequest` message.
    *
    * @param data
    * @param issuer
    * @param mDReqId
    * @param timestamp
    * @return*
    */
  def apply[T <: Tradable](data: AuctionData[T], issuer: UUID, mDReqId: UUID, timestamp: Timestamp): AuctionDataResponse[T] = {
    new AuctionDataResponse(data, issuer, Some(mDReqId), timestamp)
  }

  /** Create an unsolicited `AuctionDataResponse`.
    *
    * @param data
    * @param issuer
    * @param timestamp
    * @return*
    */
  def apply[T <: Tradable](data: AuctionData[T], issuer: UUID, timestamp: Timestamp): AuctionDataResponse[T] = {
    new AuctionDataResponse(data, issuer, None, timestamp)
  }

}


