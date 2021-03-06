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
package org.economicsl

import java.util.UUID


/** General documentation for the auctions package should go here! */
package object auctions {

  /* Type alias used to denote a unique identifier for an `Auction`. */
  type AuctionId = UUID

  type Buyer = UUID

  type Seller = UUID

  /* Type alias used to denote a unique identifier for the issuer of an order. */
  type IssuerId = UUID

}
