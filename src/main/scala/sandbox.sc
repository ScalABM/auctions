import java.util.UUID

import org.economicsl.auctions.{Currency, Price, Tradable}
import org.economicsl.auctions.random.{Random, Seed}

case class GoogleStock(tick: Currency) extends Tradable


val tick = 5
val issuer = UUID.randomUUID()
val google = GoogleStock(tick)

val seed = Seed(636413622)
val order = Random.askOrder(issuer, Price(10000), google, tick).runA(seed).value
order.limit