import org.economicsl.auctions.{Currency, Price, Tradable}
import org.economicsl.auctions.multiunit.orderbooks.OrderBook
import org.economicsl.auctions.multiunit.orders.{AskOrder, BidOrder}

import scala.collection.immutable

case class GoogleStock(tick: Currency = 1) extends Tradable

val ob = OrderBook.empty[Price, AskOrder[GoogleStock], immutable.Queue[AskOrder[GoogleStock]]]

val ob2 = OrderBook.empty[Price, AskOrder[GoogleStock], immutable.Set[AskOrder[GoogleStock]]]

val ob3 = OrderBook.empty[Price, BidOrder[GoogleStock], immutable.List[BidOrder[GoogleStock]]]
