package org.economicsl.auctions

import scala.collection.immutable


package object orderbooks {

  type SortedAskOrders[A <: LimitAskOrder] = immutable.TreeSet[A]

  type SortedBidOrders[B <: LimitBidOrder] = immutable.TreeSet[B]

}
