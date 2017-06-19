package org.economicsl.auctions.actors

import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable
import play.api.libs.json.{Json, Writes}


/** Class used to indicate that an order was rejected by an auction. */
case class Rejected(timestamp: Long, order: Order[Tradable], reason: Throwable)


object Rejected {

  implicit val writes: Writes[Rejected] = {
    o => Json.obj(
      "timestamp" -> o.timestamp,
      "order" -> o.order,
      "reason" -> o.reason.getMessage
    )
  }

}
