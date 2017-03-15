import java.util.UUID

import org.economicsl.auctions._
import org.economicsl.auctions.singleunit.{DoubleAuction, Fill}
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

// Create a single-unit market ask order...
val order3: singleunit.MarketAskOrder[Google] = singleunit.MarketAskOrder(issuer, google)

// Create a single-unit limit ask order...
val order4: singleunit.LimitAskOrder[Google] = singleunit.LimitAskOrder(issuer, Price(5.5), google)

// Create a multi-unit limit bid order...
val order5: multiunit.LimitBidOrder[Google] = multiunit.LimitBidOrder(issuer, Price(10), Quantity(100), google)

// Create a multi-unit market bid order...
val order7: multiunit.MarketBidOrder[Google] = multiunit.MarketBidOrder(issuer, Quantity(100), google)

// Create a single-unit market bid order...
val order8: singleunit.MarketBidOrder[Google] = singleunit.MarketBidOrder(issuer, google)

// Create a single-unit limit bid order...
val order9: singleunit.LimitBidOrder[Google] = singleunit.LimitBidOrder(issuer, Price(9.5), google)

// Create an order for some other tradable
val apple = new Apple()
val order10: singleunit.LimitBidOrder[Apple] = singleunit.LimitBidOrder(issuer, Price(55.9), apple)

// Create a four-heap order book and add some orders...
val orderBook = FourHeapOrderBook.empty[Google]
val orderBook2 = orderBook + order3
val orderBook3 = orderBook2 + order4
val orderBook4 = orderBook3 + order9
val orderBook5 = orderBook4 + order8

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
val (pairedOrders, _) = orderBook5.takeWhileMatched
pairedOrders.toList

//def fill[T <: Tradable](pricingRule: PricingRule[T, Price])(orderBook: FourHeapOrderBook[T]): Option[(Fill[T], FourHeapOrderBook[T])] = {
//  pricingRule(orderBook).map{ price => val (askOrder, bidOrder) = orderBook.head; Some(Fill(askOrder, bidOrder, price), orderBook.tail) }
//}

// example usage of the auction...
val auction = DoubleAuction.withEmptyOrderBook[Google]
val auction2 = auction.insert(order4)
val auction3 = auction2.insert(order9)

// thanks to @bherd-rb we can do things like this...
val (result, _) = auction3.clear(midPointPricing)
result.map(fills => fills.toList)

// ...trivial to re-run the same auction with a different pricing rule!
val (result2, _) = auction3.clear(askQuotePricing)
result2.map(fills => fills.toList)