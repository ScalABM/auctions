import java.util.UUID

import org.economicsl.auctions._

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook


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

// take a look at paired orders
val (pairedOrders, _) = orderBook5.takeWhileMatched
pairedOrders.toList


// Implement a weighted average pricing rule...
case class WeightedAveragePricing(weight: Double) extends DiscriminatoryPricingRule {

  def apply(pair: (LimitAskOrder, LimitBidOrder)): Price = pair match {
    case (askOrder, bidOrder) => Price((1 - weight) * askOrder.limit.value + weight * bidOrder.limit.value)
  }

}


// example of buyer's bid (or M+1 price rule)...incentive compatible for the seller!
pairedOrders map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, WeightedAveragePricing(1.0)((askOrder, bidOrder))) }


// example of seller's ask (or M price rule)...incentive compatible for the buyer
pairedOrders map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, WeightedAveragePricing(0.0)((askOrder, bidOrder))) }


// split the trade surplus evenly...not incentive compatible!
pairedOrders map { case (askOrder, bidOrder) => Fill(askOrder, bidOrder, WeightedAveragePricing(0.5)((askOrder, bidOrder))) }