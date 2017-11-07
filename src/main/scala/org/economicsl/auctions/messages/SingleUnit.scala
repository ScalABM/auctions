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

import org.economicsl.core.{Quantity, Tradable}


/** Mixin trait that restricts the quantity of an `Order` to a single unit of a `Tradable`.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SingleUnit[+T <: Tradable] {
  this: SinglePricePoint[T] =>

  val quantity: Quantity = Quantity.single

}