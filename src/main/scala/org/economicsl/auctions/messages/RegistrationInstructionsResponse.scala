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

  def registStatus: Char

}


/** Base trait for all `AcceptedRegistrationInstructionsResponse` messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait AcceptedRegistrationInstructions extends RegistrationInstructionsResponse {

  val registStatus: Char = 'A'

}


final case class AcceptedNewRegistrationInstructions(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends AcceptedRegistrationInstructions {

  /** Identifies registration transaction type. */
  def registTransType: Char = '0'

}


final case class AcceptedReplaceRegistrationInstructions(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends AcceptedRegistrationInstructions {

  /** Identifies registration transaction type. */
  def registTransType: Char = '1'

}


final case class AcceptedCancelRegistrationInstructions(registId: RegistrationId, registRefId: RegistrationReferenceId)
  extends AcceptedRegistrationInstructions {

  /** Identifies registration transaction type. */
  def registTransType: Char = '2'

}


final case class RejectedRegistrationInstructions(registId: RegistrationId, registRefId: String, registTransType: Char)
  extends RegistrationInstructionsResponse {

  val registStatus: Char = 'R'

}

