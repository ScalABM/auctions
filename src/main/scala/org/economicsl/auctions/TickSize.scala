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

import org.economicsl.core.Currency


/** Value class representing the tick size.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
case class TickSize(value: Currency) extends AnyVal {

  /** Computes the least common multiple of two tick sizes. */
  def leastCommonMultiple(a: TickSize, b: TickSize): TickSize = {

    @annotation.tailrec
    def gcd(a: TickSize, b: TickSize): TickSize = {
      if (b == TickSize.zero) a.abs else gcd(b, a % b)
    }

    (a.abs / gcd(a,b)) * (b.abs / gcd(a, b))  // todo check for overflow?

  }

}


object TickSize {

  implicit val tickSizeIsIntegral: Integral[TickSize] = TickSizeIsIntegral

  implicit def mkNumericOps(lhs: TickSize): TickSizeIsIntegral.Ops = {
    TickSizeIsIntegral.mkNumericOps(lhs)
  }

  val zero: TickSize = TickSize(0L)

  val one: TickSize = TickSize(1L)

}


object TickSizeIsIntegral extends Integral[TickSize] {

  def quot(x: TickSize, y: TickSize): TickSize = ???

  def rem(x: TickSize, y: TickSize): TickSize = ???

  def plus(x: TickSize, y: TickSize): TickSize = ???

  def minus(x: TickSize, y: TickSize): TickSize = ???

  def times(x: TickSize, y: TickSize): TickSize = ???

  def negate(x: TickSize): TickSize = ???

  def fromInt(x: Int): TickSize = ???

  def toInt(x: TickSize): Int = ???

  def toLong(x: TickSize): Long = ???

  def toFloat(x: TickSize): Float = ???

  def toDouble(x: TickSize): Double = ???

  def compare(x: TickSize, y: TickSize): Int = ???

}



