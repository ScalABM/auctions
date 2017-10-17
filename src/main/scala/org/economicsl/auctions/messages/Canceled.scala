package org.economicsl.auctions.messages

import org.economicsl.auctions.{Order, Token}
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Base trait for all canceled messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait Canceled
  extends Message {

  def order: Order[Tradable]

  def reason: Reason

}


/** Message used to indicate that a previously accepted order has been canceled by its issuer.
  *
  * @param timestamp
  * @param issuer the unique (to the `AuctionParticipant`) identifier of the previously accepted order.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class CanceledByIssuer(timestamp: Timestamp, issuer: Token, order: Order[Tradable])
  extends Canceled {
  val reason: Reason = IssuerRequestedCancel(order)
}
