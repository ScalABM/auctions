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


/** Base trait for all `RegistrationInstructionsResponse` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait RegistrationInstructionsResponse {

  val msgType: Char = 'p'

  /** Unique identifier of the registration details. */
  def registId: RegistrationId

  /** Identifies registration transaction type.
    *
    * @note Valid values are 0: New, 1: Replace, 2: Cancel.
    */
  def registTransType: Char

  /** Registration status as returned by the `AuctionActor`.
    *
    * @note Valid values are A: Accepted, R: Rejected
    */
  def registStatus: Char

}


/** Base trait for all `RegistrationInstructionsAccepted` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait RegistrationInstructionsAccepted extends RegistrationInstructionsResponse {

  val registStatus: Char = 'A'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `NewRegistration` has been accepted.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class NewRegistrationAccepted(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends RegistrationInstructionsAccepted {

  val registTransType: Char = '0'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `ReplaceRegistration` has been accepted.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class ReplaceRegistrationAccepted(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends RegistrationInstructionsAccepted {

  val registTransType: Char = '1'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `CancelRegistration` has been accepted.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class CancelRegistrationAccepted(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends RegistrationInstructionsAccepted {

  val registTransType: Char = '2'

}


/** Base trait for all `RegistrationInstructionsRejected` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait RegistrationInstructionsRejected extends RegistrationInstructionsResponse {

  val registStatus: Char = 'R'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `NewRegistration` has been accepted.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class NewRegistrationRejected(registId: RegistrationId)
  extends RegistrationInstructionsRejected {

  val registTransType: Char = '0'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `ReplaceRegistration` has been rejected.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class ReplaceRegistrationRejected(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends RegistrationInstructionsRejected {

  val registTransType: Char = '1'

}


/** Message sent from an `AuctionActor` to some `AuctionParticipantActor` indicating that its previously submitted
  * `CancelRegistration` has been accepted.
  *
  * @param registId unique identifier for the registration instructions assigned by the `AuctionParticipantActor`.
  * @param registRefId unique identifier for the registration instructions assigned by the `AuctionActor`.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class CancelRegistrationRejected(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends RegistrationInstructionsRejected {

  val registTransType: Char = '2'

}