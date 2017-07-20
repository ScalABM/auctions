package org.economicsl.auctions.singleunit

import org.economicsl.auctions._

import scala.collection.immutable.HashMap


class TestOrderTracker private(
  protected val outstandingOrders: Map[Token, (Reference, Contract)])
    extends OrderTracker[TestOrderTracker] {

  /** Factory method used by sub-classes to create an `A`. */
  protected def withOutstandingOrders(updated: Map[Token, (Reference, Contract)]): TestOrderTracker = {
    new TestOrderTracker(updated)
  }

}


object TestOrderTracker {

  def withNoOutstandingOrder: TestOrderTracker = {
    val emptyOutstandingOrders = HashMap.empty[Token, (Reference, Contract)]
    new TestOrderTracker(emptyOutstandingOrders)
  }

}
