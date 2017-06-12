import org.economicsl.auctions.random.{LinearCongruentialGenerator, RandomGenerator, RandomGenerator$}

val prng = LinearCongruentialGenerator(42555)

val (l, prng2) = RandomGenerator.nonNegativeMultipleOf(13)(prng)

RandomGenerator.double(prng2)

RandomGenerator.float(prng2)