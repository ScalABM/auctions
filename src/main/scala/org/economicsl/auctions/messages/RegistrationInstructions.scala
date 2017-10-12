package org.economicsl.auctions.messages


sealed trait RegistrationInstructions {

  val msgType: Char = 'o'

  /** Unique identifier of the registration details. */
  def registId: String

  /** Identifies registration transaction type. */
  def registTransType: Char

}


final case class NewRegistration(registId: String) extends RegistrationInstructions {
  val registTransType: Char = '0'
}


final case class ReplaceRegistration(registId: String, registRefId: String) extends RegistrationInstructions {
  val registTransType: Char = '1'
}


final case class CancelRegistration(registId: String, registRefId: String) extends RegistrationInstructions {
  val registTransType: Char = '2'
}

