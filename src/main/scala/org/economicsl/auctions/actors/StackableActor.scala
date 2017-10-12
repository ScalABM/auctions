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

import akka.actor.{Actor, ActorLogging}


trait StackableActor
  extends Actor
  with ActorLogging {

  /** The receive method checks if `wrappedReceive` is defined for a `message` and if so calls it. Otherwise it calls
    * `unhandled`. In other words, `receive` delegates messages to `wrappedReceive` where possible.
    */
  def receive: Receive = {
    case message if wrappedReceive.isDefinedAt(message) =>
      wrappedReceive(message)
    case message =>
      unhandled(message)
  }

  /** Assigns new partial function to variable `wrappedReceive`. */
  protected def wrappedBecome(receive: Receive): Unit = {
    wrappedReceive = receive
  }

  /** Determines the current method for handling messages.
    *
    * @note `wrappedReceive` is a variable (rather than a value) so that we can change behaviour of our actors on the
    *      fly by assigning to it different partial function to handle messages in different way.
    */
  protected var wrappedReceive: Receive = {
    case message => unhandled(message)
  }

}
