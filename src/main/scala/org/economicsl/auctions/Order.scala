package org.economicsl.auctions

import org.economicsl.core.Tradable


trait Order[+T <: Tradable] extends Contract {
  this: PriceQuantitySchedule[T] =>
}
