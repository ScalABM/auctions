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
package org.economicsl.auctions.messages


/** Base trait for all `RegistrationInstructions` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait RegistrationInstructions {

  val msgType: Char = 'o'

  /** Unique identifier of the registration details. */
  def registId: RegistrationId

  /** Identifies registration transaction type. */
  def registTransType: Char

}


/** Message sent from an `AuctionParticipantActor` to some `AuctionActor` in order to register as a participant.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class NewRegistration(registId: RegistrationId) extends RegistrationInstructions {
  val registTransType: Char = '0'
}


/** Message sent from an `AuctionParticipantActor` to some `AuctionActor` in order to replace its existing registration.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class ReplaceRegistration(registId: RegistrationId, registRefId: RegistrationReferenceId)
    extends RegistrationInstructions {
  val registTransType: Char = '1'
}


/** Message sent from an `AuctionParticipantActor` to some `AuctionActor` in order to cancel its existing registration.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class CancelRegistration(registId: RegistrationId, registRefId: RegistrationReferenceId)
    extends RegistrationInstructions {
  val registTransType: Char = '2'
}

