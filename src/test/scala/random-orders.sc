import org.economicsl.auctions.{Price, Quantity}
import org.scalacheck.{Arbitrary, Gen}


def limit(upper: Long): Gen[Price] = for {
  v <- Arbitrary.arbitrary[Long] suchThat (v => 0 < v)
} yield Price(v)


def quantity(upper: Long): Gen[Quantity] = for {
  v <- Arbitrary.arbitrary[Long] suchThat (v => 0.0 < v && v <= upper)
} yield Quantity(v)

/*
def limitAskOrder[T <: Tradable](upper: Double)(issuer: UUID, tradable: T): Gen[LimitAskOrder] = for {
  p <- limit(upper)
  q <- quantity(upper)
} yield LimitAskOrder(issuer, p, q, tradable)
*/


