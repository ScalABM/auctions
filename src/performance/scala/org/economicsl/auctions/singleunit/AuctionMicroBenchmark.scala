package org.economicsl.auctions.singleunit

import org.scalameter.Bench


/** Performance tests for the `Auction` class. */
object AuctionMicroBenchmark extends Bench.OnlineRegressionReport {

  performance of "Auction" in {

    /** Adding an `Order` to an `OrderBook` should be an `O(1)` operation. */
    measure method "insert" in {
      ???
    }

    /** Removing an `Order` from an `OrderBook` should be an `O(1)` operation. */
    measure method "remove" in {
      ???
    }

    /** Finding an `Order` in an `OrderBook` should be an `O(n)` operation. */
    measure method "find" in {
      ???
    }

  }

}