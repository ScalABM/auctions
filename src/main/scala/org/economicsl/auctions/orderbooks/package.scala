package org.economicsl.auctions

import scala.collection.immutable


package object orderbooks {

  type SortedAskOrders = immutable.TreeSet[LimitAskOrder with SingleUnit]

  type SortedBidOrders = immutable.TreeSet[LimitBidOrder with SingleUnit]

}
