package org.economicsl.auctions

import org.economicsl.core.Tradable


/** Base trait for all orders.
  *
  * @tparam T each `Order` must be issued for a particular type of `Tradable`.
  * @author davidrpugh
  * @since 0.2.0
  */
sealed trait Order[+T <: Tradable] extends Contract {
  this: PriceQuantitySchedule[T] =>
}


/** Base trait for all orders to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `AskOrder` is being issued.
  * @author davidrpugh
  * @since 0.2.0
  */
trait AskOrder[+T <: Tradable] extends Order[T] {
  this: PriceQuantitySchedule[T] =>
}


/** Base trait for all orders to buy a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `BidOrder` is being issued.
  * @author davidrpugh
  * @since 0.2.0
  */
trait BidOrder[+T <: Tradable] extends Order[T] {
  this: PriceQuantitySchedule[T] =>
}