import java.util.UUID

import org.economicsl.auctions._


/** Example `Tradable` object. */
case class Security(ticker: UUID) extends Tradable

// Create a multi-unit limit ask order...
val issuer = UUID.randomUUID()
val tradable = Security(UUID.randomUUID())
val order1: LimitAskOrder with SinglePricePoint = LimitAskOrder(issuer, Price(10), Quantity(100), tradable)

// Create a multi-unit market ask order...
val order2: MarketAskOrder with SinglePricePoint = MarketAskOrder(issuer, Quantity(100), tradable)

// Create a single-unit market ask order...
val order3: MarketAskOrder with SingleUnit = MarketAskOrder(issuer, tradable)

// Create a single-unit limit ask order...
val order4: LimitAskOrder with SingleUnit = LimitAskOrder(issuer, Price(5.5), tradable)

// Create a multi-unit limit bid order...
val order5: LimitBidOrder with SinglePricePoint = LimitBidOrder(issuer, Price(10), Quantity(100), tradable)

// Create a multi-unit market bid order...
val order7: MarketBidOrder with SinglePricePoint = MarketBidOrder(issuer, Quantity(100), tradable)

// Create a single-unit market bid order...
val order8: MarketBidOrder with SingleUnit = MarketBidOrder(issuer, tradable)

// Create a single-unit limit bid order...
val order9: LimitBidOrder with SingleUnit = LimitBidOrder(issuer, Price(5.5), tradable)
