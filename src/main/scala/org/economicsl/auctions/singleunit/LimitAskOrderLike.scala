package org.economicsl.auctions.singleunit


import org.economicsl.auctions.{AskOrder, Tradable}


trait LimitAskOrderLike[+T <: Tradable] extends AskOrder[T] with SingleUnit[T]


object LimitAskOrderLike {

  implicit def ordering[O <: LimitAskOrderLike[_ <: Tradable]]: Ordering[O] = SingleUnit.ordering[O]

}
