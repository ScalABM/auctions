/*
Copyright 2017 EconomicSL

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
package org.economicsl.auctions.multiunit

import org.economicsl.auctions.{Quantity, Tradable}

import scala.collection.GenIterable


/** Documentation for multi-unit orderbooks goes here! */
package object orderbooks {

  def totalQuantity[T <: Tradable](orders: GenIterable[Order[T]]): Quantity = {
    orders.aggregate[Quantity](Quantity(0))((total, order) => Quantity(total.value + order.quantity.value), (q1, q2) => Quantity(q1.value + q2.value))
  }

}
