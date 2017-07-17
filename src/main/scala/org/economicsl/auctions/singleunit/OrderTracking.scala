package org.economicsl.auctions.singleunit

import org.economicsl.auctions.actors.{Reference, Timestamp, Token}
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.Tradable


trait OrderTracking {

  protected def outstandingOrders: Map[Token, (Reference, Order[Tradable])]

}



object OrderTracking {

  /** Message used to indicate that a previously accepted order has been canceled.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Canceled(timestamp: Timestamp, token: Token, order: Order[Tradable], reason: Reason)


  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Timestamp, token: Token, order: Order[Tradable], reference: Reference)


  /** Message used to indicate that a previously submitted order has been rejected.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Rejected(timestamp: Timestamp, token: Token, order: Order[Tradable], reason: Reason)

  sealed trait Reason {

    def message: String

  }

  final case class InvalidTickSize(order: Order[_ <: Tradable], tickSize: Long) extends Reason {
    val message: String = s"Limit price of ${order.limit} is not a multiple of the tick size $tickSize"
  }

}
