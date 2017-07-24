package org.economicsl.auctions.singleunit.orders

import org.economicsl.auctions.{Issuer, Order, SingleUnit}
import org.economicsl.core.{Price, Tradable}



/** Base trait for all single-unit order implementations.
  *
  * @tparam T
  */
sealed trait SingleUnitOrder[+T <: Tradable] extends Order[T] with SingleUnit[T]


/** Companion object for the `SingleUnitOrder` trait.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitOrder {

  /** All `SingleUnitOrder` instances are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `SingleUnitOrder` that is being ordered.
    * @return `Ordering` defined over `SingleUnitOrder` instances.
    */
  def ordering[T <: Tradable, O <: SingleUnitOrder[T]]: Ordering[O] = {
    Ordering.by(o => (o.limit, o.issuer)) // todo re-visit whether or not issuer can only have a single active order!
  }

}


/** Class representing a single-unit order to sell a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `AskOrder` is being issued.
  * @author davidrpugh
  * @since 0.2.0
  */
case class SingleUnitAskOrder[+T <: Tradable](issuer: Issuer, limit: Price, tradable: T)
  extends SingleUnitOrder[T]


/** Companion object for `SingleUnitAskOrder`.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitAskOrder {

  def apply[T <: Tradable](issuer: Issuer, tradable: T): SingleUnitAskOrder[T] = {
    new SingleUnitAskOrder(issuer, Price.MinValue, tradable)
  }

}


/** Class representing a single-unit orders to buy a particular `Tradable`.
  *
  * @tparam T the type of `Tradable` for which the `BidOrder` is being issued.
  * @author davidrpugh
  * @since 0.1.0
  */
case class SingleUnitBidOrder[+T <: Tradable](issuer: Issuer, limit: Price, tradable: T)
  extends SingleUnitOrder[T]


/** Companion object for SingleUnitBidOrder.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
object SingleUnitBidOrder {

  def apply[T <: Tradable](issuer: Issuer, tradable: T): SingleUnitBidOrder[T] = {
    new SingleUnitBidOrder(issuer, Price.MinValue, tradable)
  }

}