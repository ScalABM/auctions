package org.economicsl.auctions

import org.economicsl.auctions.messages.NewSingleUnitOrder
import org.economicsl.core.Tradable


trait GenSingleUnitAuction[T <: Tradable, OB <: OrderBook[T, NewSingleUnitOrder[T], OB]]
  extends GenAuction[T, NewSingleUnitOrder[T], OB, GenSingleUnitAuction[T, OB]]
