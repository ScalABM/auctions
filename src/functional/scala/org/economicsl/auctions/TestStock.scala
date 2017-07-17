package org.economicsl.auctions

import org.economicsl.core.securities.Stock


case class TestStock() extends Stock {
  val ticker: String = "GOOG"
}
