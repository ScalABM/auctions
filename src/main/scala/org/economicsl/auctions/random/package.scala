package org.economicsl.auctions

import java.util.UUID

import cats.data.State
import org.economicsl.auctions.singleunit.orders.{LimitAskOrder, LimitBidOrder, Order}


/** Package containing classes and methods for generating random data structures for auction simulations. */
package object random {

  type Random[A] = State[Seed, A]

}
