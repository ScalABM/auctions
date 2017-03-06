package org.economicsl.auctions

import scala.collection.immutable


package object orderbooks {

  type SortedAskOrders[T <: Tradable, A <: LimitAskOrder[T]] = immutable.TreeSet[A]

  type SortedBidOrders[T <: Tradable, B <: LimitBidOrder[T]] = immutable.TreeSet[B]

}
