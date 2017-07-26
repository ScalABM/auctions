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

import org.economicsl.core.{Currency, Tradable}


/** Need some data structure to convey the information about an auction to participants. */
trait AuctionProtocol[+T <: Tradable] {

  def tickSize: Currency

  def tradable: T

  def withTickSize(updated: Currency): AuctionProtocol[T]

}


object AuctionProtocol {

  def apply[T <: Tradable](tickSize: Currency, tradable: T): AuctionProtocol[T] = {
    AuctionProtocolImpl(tickSize, tradable)
  }

  def apply[T <: Tradable](tradable: T): AuctionProtocol[T] = {
    AuctionProtocolImpl(1L, tradable)
  }

  private case class AuctionProtocolImpl[+T <: Tradable](tickSize: Currency, tradable: T)
      extends AuctionProtocol[T] {

    def withTickSize(updated: Currency): AuctionProtocol[T] = {
      AuctionProtocolImpl(updated, tradable)
    }

  }

}