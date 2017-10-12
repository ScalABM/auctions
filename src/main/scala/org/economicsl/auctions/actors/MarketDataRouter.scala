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
package org.economicsl.auctions.actors

import akka.actor.Terminated
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import org.economicsl.auctions.messages.{MarketDataSubscribe, MarketDataUnsubscribe}
import org.economicsl.auctions.singleunit.Auction
import org.economicsl.core.Tradable


/** Mixin trait that provides support for handling market data requests. */
trait MarketDataRouter[T <: Tradable, A <: Auction[T, A]]
    extends StackableActor {
  this: AuctionActor[T, A] =>

  override def receive: Receive = {
    case message: MarketDataSubscribe =>
      context.watch(sender())
      ticker = ticker.addRoutee(sender())
      super.receive(message)
    case message: MarketDataUnsubscribe =>
      context.unwatch(sender())
      ticker = ticker.removeRoutee(sender())
      super.receive(message)
    case message @ Terminated(participant) =>
      context.unwatch(participant)
      ticker = ticker.removeRoutee(participant)
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /* `Router` will broadcast messages to all registered auction participants (even if participants are remote!) */
  protected var ticker: Router = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee])

}
