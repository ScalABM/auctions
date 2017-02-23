import java.util.UUID

import org.economicsl.auctions.{Order, Price, Quantity}

import scala.collection.{GenIterable, immutable}


/** Mixin trait providing a schedule of price-quantity pairs for an order. */
trait PriceQuantitySchedule {
  this: Order =>

  type PricePoint = (Price, Quantity)

  /** A schedule is a step-wise specification of an `Order` to buy (or sell) various quantities
    * of a `Tradable` at specific, discrete price-points.
    */
  def schedule: GenIterable[PricePoint]

}


/** Mixin trait defining an `Order` for multiple units of a `Tradable` at some limit price. */
trait SinglePricePoint extends PriceQuantitySchedule {
  this: Order =>

  /** Limit price (per unit of the `Tradable`) for the Order.
    *
    * The limit price imposes an upper (lower) bound on a bid (ask) order.
    */
  def limit: Price

  /** Desired number of units of the `Tradable`. */
  def quantity: Quantity

  val schedule: immutable.Map[Price, Quantity] = immutable.Map(limit -> quantity)

}


/** Companion object for the `SinglePricePoint` trait.
  *
  * Defines a basic ordering for anything that mixes in the `SinglePricePoint` trait.
  */
object SinglePricePoint {

  /** All `Order` instances that mixin `SinglePricePoint` are ordered by `limit` from lowest to highest.
    *
    * @tparam O the sub-type of `Order with SinglePricePoint` that is being ordered.
    * @return and `Ordering` defined over `Order with SinglePricePoint` instances.
    */
  def ordering[O <: Order with SinglePricePoint]: Ordering[O] = Ordering.by(o => (o.limit, o.issuer))

}


/** Mixin trait defining an `Order` for a single unit of a `Tradable` at some limit price. */
trait SingleUnit extends SinglePricePoint {
  this: Order =>

  val quantity = Quantity(1)

}


/** Base trait for a limit order to sell some `Tradable`. */
trait LimitAskOrder extends AskOrder


/** Companion object for `LimitAskOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `LimitAskOrder` trait.
  */
object LimitAskOrder {

  implicit def ordering[O <: LimitAskOrder with SinglePricePoint]: Ordering[O] = SinglePricePoint.ordering[O]

  def apply(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable): LimitAskOrder with SinglePricePoint = {
    SinglePricePointImpl(issuer, limit, quantity, tradable)
  }

  def apply(issuer: UUID, limit: Price, tradable: Tradable): LimitAskOrder with SingleUnit = {
    SingleUnitImpl(issuer, limit, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable)
    extends LimitAskOrder with SinglePricePoint

  private[this] case class SingleUnitImpl(issuer: UUID, limit: Price, tradable: Tradable)
    extends LimitAskOrder with SingleUnit

}


/** Base trait for a market order to sell some `Tradable`. */
trait MarketAskOrder extends LimitAskOrder {

  /** An issuer of a `MarketAskOrder` is willing to sell at any strictly positive price. */
  val limit: Price = Price.MinPositiveValue

}

/** Companion object for `MarketAskOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `MarketAskOrder` trait.
  */
object MarketAskOrder {

  def apply(issuer: UUID, quantity: Quantity, tradable: Tradable): MarketAskOrder with SinglePricePoint = {
    SinglePricePointImpl(issuer, quantity, tradable)
  }

  def apply(issuer: UUID, tradable: Tradable): MarketAskOrder with SingleUnit = {
    SingleUnitImpl(issuer, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, quantity: Quantity, tradable: Tradable)
    extends MarketAskOrder with SinglePricePoint

  private[this] case class SingleUnitImpl(issuer: UUID, tradable: Tradable) extends MarketAskOrder with SingleUnit

}


/** Base trait for a limit order to buy some `Tradable`. */
trait LimitBidOrder extends BidOrder


/** Companion object for `LimitBidOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `LimitBidOrder` trait.
  */
object LimitBidOrder {

  implicit def ordering[O <: LimitBidOrder with SinglePricePoint]: Ordering[O] = SinglePricePoint.ordering[O].reverse

  def apply(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable): LimitBidOrder with SinglePricePoint = {
    SinglePricePointImpl(issuer, limit, quantity, tradable)
  }

  def apply(issuer: UUID, limit: Price, tradable: Tradable): LimitBidOrder with SingleUnit = {
    SingleUnitImpl(issuer, limit, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, limit: Price, quantity: Quantity, tradable: Tradable)
    extends LimitBidOrder with SinglePricePoint

  private[this] case class SingleUnitImpl(issuer: UUID, limit: Price, tradable: Tradable)
    extends LimitBidOrder with SingleUnit

}


/** Base trait for a market order to buy a particular `Tradable`. */
trait MarketBidOrder extends LimitBidOrder {

  /** An issuer of a `MarketBidOrder` is willing to pay any finite price. */
  val limit: Price = Price.MaxValue

}

/** Companion object for `MarketBidOrder`.
  *
  * Provides default ordering as well as constructors for default implementations of `MarketBidOrder` trait.
  */
object MarketBidOrder {

  def apply(issuer: UUID, quantity: Quantity, tradable: Tradable): MarketBidOrder with SinglePricePoint = {
    SinglePricePointImpl(issuer, quantity, tradable)
  }

  def apply(issuer: UUID, tradable: Tradable): MarketBidOrder with SingleUnit = {
    SingleUnitImpl(issuer, tradable)
  }

  private[this] case class SinglePricePointImpl(issuer: UUID, quantity: Quantity, tradable: Tradable)
    extends MarketBidOrder with SinglePricePoint

  private[this] case class SingleUnitImpl(issuer: UUID, tradable: Tradable) extends MarketBidOrder with SingleUnit

}