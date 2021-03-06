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

import java.util.UUID


/** Package containing traits and classes for implementing a messaging protocol for auction applications.
  *
  * Many examples of electronic trading protocols exist. See
  * [Wikipedia https://en.wikipedia.org/wiki/List_of_electronic_trading_protocols] for some examples.
  *
  */
package object messages {

  /* Unique reference identifier for an accepted order as assigned by an `Auction`. */
  type OrderReferenceId = UUID

  /* Unique identifier for an order assigned by an `AuctionParticipant`. */
  type OrderId = UUID

  /** Unique reference identifier for the registration details as assigned by the `AuctionActor`. */
  type RegistrationReferenceId = UUID

  /** Unique identifier of the registration details as assigned by the `AuctionParticipantActor`. */
  type RegistrationId = UUID

  /** Unique identifier of the sender of a particular `Message`. */
  type SenderId = UUID

}
