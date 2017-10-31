package org.economicsl.auctions

import org.economicsl.auctions.messages.Message
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Message used to indicate that a previously submitted order was accepted.
  *
  * @param timestamp
  * @param issuer the unique (to the `AuctionParticipant`) identifier of the previously accepted order.
  * @param order the previously submitted order that has been accepted.
  * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
  * @author davidrpugh
  * @since 0.2.0
  */
final case class Accepted(
                           timestamp: Timestamp,
                           issuer: Token, order: Order[Tradable],
                           reference: Reference)
  extends Message {

  val kv: (Token, (Reference, Order[Tradable])) = issuer -> (reference -> order)

}
