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
package org.economicsl.auctions.actors.schedules

import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import scala.util.Random


/** Mixin trait providing requirements for defining a Poisson process.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait PoissonProcess {

  /** The mean arrival rate will be 1 / lambda. */
  def lambda: Double

  /** Source of randomness. */
  def prng: Random

  /** Specifies the time unit used to define the interval. */
  def timeUnit: TimeUnit

  def delay: FiniteDuration = {
    val deltaT = inverseCumulativeDistributionFunction(lambda, prng.nextDouble())
    FiniteDuration(deltaT.toLong, timeUnit)
  }

  /** Inverse cumulative distribution function for an exponential random variable. */
  private[this] def inverseCumulativeDistributionFunction(lambda: Double, quantile: Double): Double = {
    -math.log(1 - quantile) / lambda
  }

}
