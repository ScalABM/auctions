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


final case class NewRegistration(registId: RegistrationId) extends RegistrationInstructions {
  val registTransType: Char = '0'
}


final case class ReplaceRegistration(registId: RegistrationId, registRefId: RegistrationReferenceId)
    extends RegistrationInstructions {
  val registTransType: Char = '1'
}


final case class CancelRegistration(registId: RegistrationId, registRefId: RegistrationReferenceId)
    extends RegistrationInstructions {
  val registTransType: Char = '2'
}

