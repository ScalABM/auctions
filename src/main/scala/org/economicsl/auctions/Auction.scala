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

import org.economicsl.auctions.participants.Token
import org.economicsl.core.util.Timestamper
import org.economicsl.core.{Currency, Tradable}


/** Base trait for all auction implementations.
  *
  * @tparam T
  * @tparam A
  * @note Note the use of F-bounded polymorphism over Type classes. We developed an alternative implementation using the
  *       Type class pattern that was quite elegant, however Type classes can not be used directly from Java. In order
  *       to use the Type class implementation from Java, we would need to develop (and maintain!) separate wrappers for
  *       each auction implementation.
  */
trait Auction[T <: Tradable, O <: Order[T], +A <: Auction[T, O, A]]
    extends ReferenceGenerator
    with Timestamper {
  this: A =>

  import org.economicsl.auctions.participants.OrderTracker._

  def cancel(reference: Reference): (A, Option[Canceled])

  def clear: (A, Option[Stream[SpotContract]])

  def insert(kv: (Token, O)): (A, Either[Rejected, Accepted])

  def tickSize: Currency

  def tradable: T

  /** Returns an auction of type `A` the encapsulates the current auction state but with a new tick size. */
  def withTickSize(updated: Currency): A

}


object Auction {

  /** Need some data structure to convey the relevant information about an auction to participants. */
  final case class AuctionProtocol(tickSize: Currency, tradable: Tradable)

}

