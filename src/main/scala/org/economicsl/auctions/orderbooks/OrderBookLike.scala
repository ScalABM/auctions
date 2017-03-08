package org.economicsl.auctions.orderbooks

import org.economicsl.auctions.{Order, Tradable}

import scala.collection.GenIterable


trait OrderBookLike[+O <: Order[_ <: Tradable]] {

  /** Filter the `OrderBook` and return those `Order` instances satisfying the given predicate.
    *
    * @param p predicate defining desirable `Order` characteristics.
    * @return `None` if no `Order` instance satisfies `p`; otherwise, some `Order` instances.
    */
  def filter(p: (O) => Boolean): Option[GenIterable[O]]

  /** Find the first `Order` in the `OrderBook` that satisfies the given predicate.
    *
    * @param p predicate defining desirable `Order` characteristics.
    * @return `None` if no `Order` in the `OrderBook` satisfies the predicate; `Some(order)` otherwise.
    */
  def find(p: (O) => Boolean): Option[O]

  /** Return the head `Order` of the `OrderBook`.
    *
    * @return `None` if the `OrderBook` is empty; `Some(order)` otherwise.
    */
  def headOption: Option[O]

  /** Boolean flag indicating whether or not the `OrderBook` contains `Order` instances.
    *
    * @return `true`, if the `OrderBook` does not contain any `Order` instances; `false`, otherwise.
    */
  def isEmpty: Boolean

  /** Boolean flag indicating whether or not the `OrderBook` contains `Order` instances.
    *
    * @return `true`, if the `OrderBook` contains any `Order` instances; `false`, otherwise.
    */
  def nonEmpty: Boolean

  /** Applies a binary operator to a start value and all existing orders of the `OrderBook`, going left to right.
    *
    * @tparam P the return type of the binary operator
    */
  def foldLeft[P](z: P)(op: (P, O) => P): P

  /** Reduces the existing orders of this `OrderBook`, if any, using the specified associative binary operator.
    *
    * @param op an associative binary operator.
    * @return `None` if the `OrderBook` is empty; the result of applying the `op` to the existing orders in the
    *         `OrderBook` otherwise.
    * @note reducing the existing orders of an `OrderBook` is an `O(n)` operation. The order in which operations are
    *       performed on elements is unspecified and may be nondeterministic depending on the type of `OrderBook`.
    */
  def reduceOption[P >: O](op: (P, P) => P): Option[P]

}
