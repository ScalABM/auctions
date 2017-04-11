package org.economicsl.auctions.singleunit

import org.scalameter.{Bench, Gen}


/** Performance tests for the `Auction` class. */
object AuctionMicroBenchmark extends Bench.OnlineRegressionReport with OrderGenerator {

  val sizes = Gen.exponential("Number of existing orders")(factor=10, until=1000000, from=10)

  /** Generates a collection of OrderBooks of increasing size. */
  val auction = for { size <- sizes } yield {
    val orderBook = OrderBook[AskOrder with Persistent](validTradable)
    val orders = for { i <- 1 to size } yield orderGenerator.nextAskOrder(0.5, validTradable)
    orders.foreach( order => orderBook.add(order) )
    orderBook
  }

  performance of "Auction" config (
    //reports.resultDir -> "target/benchmarks/markets/engines/orderbooks/OrderBook",
    exec.benchRuns -> 200,
    exec.independentSamples -> 20,
    exec.jvmflags -> List("-Xmx2G")
  ) in {

    /** Adding an `Order` to an `OrderBook` should be an `O(1)` operation. */
    measure method "insert" in {
      using(orderBooks) in {
        orderBook =>
          val newOrder = orderGenerator.nextAskOrder(0.5, validTradable)
          orderBook.add(newOrder)
      }
    }

    /** Removing an `Order` from an `OrderBook` should be an `O(1)` operation. */
    measure method "remove" in {
      using(orderBooks) in {
        orderBook =>
          val (uuid, _) = orderBook.existingOrders.head
          orderBook.remove(uuid)
      }
    }

    /** Finding an `Order` in an `OrderBook` should be an `O(n)` operation. */
    measure method "find" in {
      using(orderBooks) in {
        orderBook => orderBook.find(order => order.isInstanceOf[LimitAskOrder])
      }
    }

  }

}