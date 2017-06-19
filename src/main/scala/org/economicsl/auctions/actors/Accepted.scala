package org.economicsl.auctions.actors

import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable
import play.api.libs.json.{Json, Writes}


/** Class used to indicate that an order was accepted by an auction.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
case class Accepted(timestamp: Long, order: Order[Tradable])


object Accepted {

  implicit val writes: Writes[Accepted] = Json.writes[Accepted]

}