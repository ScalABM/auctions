package org.economicsl.auctions

import org.economicsl.auctions.messages.{NewOrderAccepted, NewOrderRejected, NewSinglePricePointOrder}
import org.economicsl.core.Tradable


/**
  * Created by pughdr on 11/23/2017.
  */
trait GenSinglePricePointAuction[T <: Tradable] extends GenSingleUnitAuction[T] {

  /** Create a new instance of auction type `A` whose order book contains an additional order of type `O`.
    *
    * @param message
    * @return
    */
  def insert(message: NewSinglePricePointOrder[T]): (GenSinglePricePointAuction[T], Either[NewOrderRejected, NewOrderAccepted])

}
