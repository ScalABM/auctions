/*
Copyright 2016 David R. Pugh

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

import java.util.UUID

import akka.actor.{DiagnosticActorLogging, Props}
import org.economicsl.auctions.TokenGenerator


class TestOrderTracker private(val issuer: UUID)
    extends StackableActor
    with DiagnosticActorLogging
    with OrderTrackingActor
    with TokenGenerator {

  wrappedBecome(trackingOrders)


}


object TestOrderTracker {

  def props(issuer: UUID): Props = {
    Props(new TestOrderTracker(issuer))
  }

}
