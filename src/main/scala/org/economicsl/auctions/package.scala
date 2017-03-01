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
package org.economicsl

import scala.language.implicitConversions


package object auctions {

  /** Type used to indicate that something is a contract. */
  trait Contract

  /** Type used to indicate that an object can be traded via an auction. */
  trait Tradable

  /** Type used to representing currencies. */
  type Currency = Double  // todo should this be Long or Double?


  /** Value class representing prices. */
  case class Price(value: Currency) extends AnyVal


  /** Companion object for the Price value class. */
  object Price {

    /** Default ordering for `Price` instances is low to high based on the underlying value. */
    implicit val ordering: Ordering[Price] = PriceOrdering

    implicit def mkOrderingOps(lhs: Price): PriceOrdering.Ops = PriceOrdering.mkOrderingOps(lhs)

    val MaxValue = Price(Double.MaxValue)  // todo this number might be hardware specific?

    val MinValue = Price(0.0)  // todo Wellman et al use negative prices to indicate sell orders?

    val MinPositiveValue = Price(Double.MinPositiveValue)  // todo this number might be hardware specific?

  }


  /** Default ordering for `Price` instances is low to high based on the underlying value. */
  object PriceOrdering extends Ordering[Price] {

    /** Instances of `Price` are compared using their underlying values.
      *
      * @param p1 some `Price` instance.
      * @param p2 another `Price` instance.
      * @return -1 if `p1` is less than `p2`, 0 if `p1` equals `p2`, 1 otherwise.
      */
    def compare(p1: Price, p2: Price): Int = p1.value compare p2.value

  }


  /** Value class representing quantities. */
  case class Quantity(value: Double) extends AnyVal  // todo should this be Long or Double?

}
