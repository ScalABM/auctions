package org.economicsl.auctions.messages

import org.economicsl.auctions.Token
import org.economicsl.core.util.Timestamp


/** Base trait for all trading messages.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait TradingMessage
  extends Serializable {

  def timestamp: Timestamp

  /** The unique (to the sending `AuctionParticipant`) identifier of the previously accepted order. */
  def token: Token

}
