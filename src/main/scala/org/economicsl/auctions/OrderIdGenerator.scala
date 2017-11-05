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

import org.economicsl.auctions.messages.OrderId
import org.economicsl.core.util.UUIDGenerator


/** Mixin trait providing `OrderId` methods for generating `OrderId` instances.
  *
  * @tparam P
  * @author davidrpugh
  * @since 0.2.0
  */
trait OrderIdGenerator[+P <: AuctionParticipant[P]]
    extends UUIDGenerator {
  this: P =>

  protected def randomOrderId(): OrderId = {
    randomUUID()
  }

}
