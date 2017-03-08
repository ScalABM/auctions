package org.economicsl.auctions.orderbooks

import java.util.UUID

import org.economicsl.auctions.{Order, Tradable}


trait OrderBook[O <: Order[_ <: Tradable]] extends OrderBookLike[O] {

  def + (order: O): OrderBook[O]

  def - (issuer: UUID): OrderBook[O]

}
