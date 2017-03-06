import java.util.UUID

import org.economicsl.auctions.{LimitAskOrder, Price, Quantity, Tradable}
import org.scalacheck.{Arbitrary, Gen}


def limit(upper: Double): Gen[Price] = for {
  v <- Arbitrary.arbitrary[Double] suchThat (v => 0.0 < v)
} yield Price(v)


def quantity(upper: Double): Gen[Quantity] = for {
  v <- Arbitrary.arbitrary[Double] suchThat (v => 0.0 < v && v <= upper)
} yield Quantity(v)


def limitAskOrder[T <: Tradable](upper: Double)(issuer: UUID, tradable: T): Gen[LimitAskOrder] = for {
  p <- limit(upper)
  q <- quantity(upper)
} yield LimitAskOrder(issuer, p, q, tradable)


limit(1e6).sample
