package org.economicsl.auctions.messages


sealed trait RegistrationInstructionsResponse {

  val msgType: Char = 'p'

  /** Unique identifier of the registration details. */
  def registId: String

  /** Identifies registration transaction type. */
  def registTransType: Char

  def registStatus: String

}


final case class AcceptedRegistration(registId: String, registTransType: Char)
  extends RegistrationInstructionsResponse {

  val registStatus: String = "Accepted"

}


final case class RejectedRegistration(registId: String, registRefId: String, registTransType: Char)
  extends RegistrationInstructionsResponse {

  val registStatus: String = "Rejected"

}

