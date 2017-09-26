package org.economicsl.auctions.messages

import org.economicsl.auctions.{Order, Token}
import org.economicsl.core.Tradable
import org.economicsl.core.util.Timestamp


/** Message used to indicate that a previously submitted order has been rejected.
  *
  * @param timestamp
  * @param token the unique (to the `AuctionParticipant`) identifier of the previously accepted order.
  * @param reason
  * @author davidrpugh
  * @since 0.2.0
  */
final case class Rejected(timestamp: Timestamp, token: Token, order: Order[Tradable], reason: Reason)
  extends TradingMessage

