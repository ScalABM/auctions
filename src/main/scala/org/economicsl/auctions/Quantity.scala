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
package org.economicsl.auctions


/** Value class representing quantities. */
case class Quantity(value: Long) extends AnyVal {

  def + (that: Quantity): Quantity = {
    Quantity(value + that.value)
  }

  def - (that: Quantity): Quantity = {
    Quantity(value - that.value)
  }

}


/** Companion object for the `Quantity` value class. */
object Quantity {

  implicit val ordering: Ordering[Quantity] = QuantityOrdering

  implicit def mkOrderingOps(lhs: Quantity): QuantityOrdering.Ops = QuantityOrdering.mkOrderingOps(lhs)

}


/** Object containing the numeric operators for the `Quantity` value class. */
object QuantityOrdering extends Ordering[Quantity] {

  def compare(x: Quantity, y: Quantity): Int = x.value compare y.value

}
