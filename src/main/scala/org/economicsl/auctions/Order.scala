package org.economicsl.auctions

import org.economicsl.core.Tradable


/** Base trait for all orders.
  *
  * @tparam T each `Order` must be issued for a particular type of `Tradable`.
  * @author davidrpugh
  * @since 0.2.0
  */
trait Order[+T <: Tradable] extends Contract {
  this: PriceQuantitySchedule[T] =>

  def tradable: T

}