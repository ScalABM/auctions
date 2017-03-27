import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.{Auction, DoubleAuction}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.auctions.singleunit.pricing._


/** Example `Tradable` object. */
trait Security extends Tradable

class Google extends Security

class Apple extends Security

// Create a multi-unit limit ask order...
val issuer = UUID.randomUUID()
val google = new Google()
val order1: multiunit.LimitAskOrder[Google] = multiunit.LimitAskOrder(issuer, Price(10), Quantity(100), google)
order1.value

// Create a multi-unit market ask order...
val order2: multiunit.MarketAskOrder[Google] = multiunit.MarketAskOrder(issuer, Quantity(100), google)

// Create some single-unit limit ask orders...
val order3: singleunit.LimitAskOrder[Google] = singleunit.LimitAskOrder(issuer, Price(5.0), google)
val order4: singleunit.LimitAskOrder[Google] = singleunit.LimitAskOrder(issuer, Price(6.0), google)

// Create a multi-unit limit bid order...
val order5: multiunit.LimitBidOrder[Google] = multiunit.LimitBidOrder(issuer, Price(10), Quantity(100), google)

// Create a multi-unit market bid order...
val order7: multiunit.MarketBidOrder[Google] = multiunit.MarketBidOrder(issuer, Quantity(100), google)

// Create some single-unit limit bid orders...
val order8: singleunit.LimitBidOrder[Google] = singleunit.LimitBidOrder(issuer, Price(10.0), google)
val order9: singleunit.LimitBidOrder[Google] = singleunit.LimitBidOrder(issuer, Price(6.0), google)

// Create an order for some other tradable
val apple = new Apple()
val order10: singleunit.LimitBidOrder[Apple] = singleunit.LimitBidOrder(issuer, Price(55.9), apple)

// Create a four-heap order book and add some orders...
val orderBook = FourHeapOrderBook.empty[Google]
val orderBook2 = orderBook + order3
val orderBook3 = orderBook2 + order4
val orderBook4 = orderBook3 + order9
val orderBook5 = orderBook4 + order8

val (matchedOrders, _) = orderBook5.takeAllMatched
matchedOrders.toList

// this should not compile...and it doesn't!
// orderBook5 + order10

// example of a uniform price auction that would be incentive compatible for the sellers...
val askQuotePricing = new AskQuotePricingRule[Google]()
val price1 = askQuotePricing(orderBook5)

// example of a uniform price auction that would be incentive compatible for the buyers...
val bidQuotePricing = new BidQuotePricingRule[Google]()
val price2 = bidQuotePricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val midPointPricing = new MidPointPricingRule[Google]
val midPrice = midPointPricing(orderBook5)

// example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
val averagePricing = new WeightedAveragePricingRule[Google](0.75)
val averagePrice = averagePricing(orderBook5)

// take a look at paired orders
val (pairedOrders, _) = orderBook5.takeAllMatched
pairedOrders.toList

// example usage of a double auction with uniform pricing...
val auction = DoubleAuction.withUniformPricing[Google]
val auction2 = auction.insert(order3)
val auction3 = auction2.insert(order4)
val auction4 = auction3.insert(order9)
val auction5 = auction4.insert(order8)

// thanks to @bherd-rb we can do things like this...
val (result, _) = auction5.clear(midPointPricing)
result.map(fills => fills.map(fill => fill.price).toList)

// ...trivial to re-run the same auction with a different pricing rule!
val (result2, _) = auction5.clear(askQuotePricing)
result2.map(fills => fills.map(fill => fill.price).toList)


// example usage of a double auction with discriminatory pricing...
val auction6 = DoubleAuction.withDiscriminatoryPricing[Google]
val auction7 = auction6.insert(order3)
val auction8 = auction7.insert(order4)
val auction9 = auction8.insert(order9)
val auction10 = auction9.insert(order8)

// thanks to @bherd-rb we can do things like this...
val (result3, _) = auction10.clear(midPointPricing)
result3.map(fills => fills.map(fill => fill.price).toList)

// ...trivial to re-run the same auction with a different pricing rule!
val (result4, _) = auction10.clear(bidQuotePricing)
result4.map(fills => fills.map(fill => fill.price).toList)


// example of a first-price auction for some tradable...
class Painting extends Tradable

val otherIssuer = UUID.randomUUID()
val picasso = new Painting  // how do we know auction is for this particular Painting instance! Probably requires run-time check!
val reservationPrice = singleunit.LimitAskOrder(otherIssuer, Price(1e7), picasso)

val lowBidder = UUID.randomUUID()
val lowBidOrder = singleunit.LimitBidOrder(lowBidder, Price(1.5e7), picasso)

val highBidder = UUID.randomUUID()
val highBidOrder = singleunit.LimitBidOrder(highBidder, Price(1e8), picasso)

val otherAuction = Auction(reservationPrice)
val otherAuction2 = otherAuction.insert(lowBidOrder)
val otherAuction3 = otherAuction2.insert(highBidOrder)

// there is a BUG somewhere because the highBidOrder should win!
val highestPriceWins = new AskQuotePricingRule[Painting]
val (results, _) = otherAuction3.clear(highestPriceWins)
results.map(fills => fills.map(fill => fill.price).toList)
