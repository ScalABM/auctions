package org.economicsl.auctions.singleunit

import org.economicsl.auctions.{Price, Tradable}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing.PricingRule


class FirstPriceAuction[T <: Tradable] private(orderBook: FourHeapOrderBook[T]) extends DoubleAuction[T] {

  def insert(order: LimitAskOrder[T]): DoubleAuction[T] = ???

  def insert(order: LimitBidOrder[T]): DoubleAuction[T] = ???

  def remove(order: LimitAskOrder[T]): DoubleAuction[T] = ???

  def remove(order: LimitBidOrder[T]): DoubleAuction[T] = ???

  def clear(p: PricingRule[T, Price]): (Option[Stream[Fill[T]]], DoubleAuction[T]) = {
    ???
  }

}
