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


/** Value class representing prices.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
case class Price(value: Currency) extends AnyVal {

  def + (that: Price): Price = {
    Price(value + that.value)
  }

}


/** Companion object for the Price value class.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object Price {

  /** Default ordering for `Price` instances is low to high based on the underlying value. */
  implicit val ordering: Ordering[Price] = PriceOrdering

  implicit def mkOrderingOps(lhs: Price): PriceOrdering.Ops = PriceOrdering.mkOrderingOps(lhs)

  val MaxValue = Price(Long.MaxValue)

  val MinValue = Price(0)  // this is not restricting quantities to be non-negative!

}


/** Default ordering for `Price` instances is low to high based on the underlying value.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
object PriceOrdering extends Ordering[Price] {

  /** Instances of `Price` are compared using their underlying values.
    *
    * @param p1 some `Price` instance.
    * @param p2 another `Price` instance.
    * @return -1 if `p1` is less than `p2`, 0 if `p1` equals `p2`, 1 otherwise.
    */
  def compare(p1: Price, p2: Price): Int = p1.value compare p2.value

}

