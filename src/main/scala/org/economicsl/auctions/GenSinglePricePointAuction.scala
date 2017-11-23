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

import org.economicsl.auctions.messages.{NewOrderAccepted, NewOrderRejected, NewSinglePricePointOrder}
import org.economicsl.core.Tradable


trait GenSinglePricePointAuction[T <: Tradable, OB <: OrderBook[T, NewSinglePricePointOrder[T], OB], A <: GenSinglePricePointAuction[T, OB, A]]
  extends GenSingleUnitAuction[T, OB, A] {
  this: A =>

  /**
    *
    * @param message
    * @return
    */
  override def insert(message: NewSinglePricePointOrder[T]): (A, Either[NewOrderRejected, NewOrderAccepted]) = {
    super.insert(message)
  }

}
