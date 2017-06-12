package org.economicsl.auctions.random


trait RandomGenerator {

  def nextInt: (Int, RandomGenerator)

  def nextLong: (Long, RandomGenerator)

}


object RandomGenerator {

  val double: Random[Double] = {
    map(nonNegativeInt)(i => i / (Int.MaxValue.toDouble + 1))
  }

  val float: Random[Float] = {
    map(nonNegativeInt)(i => i / (Int.MaxValue.toFloat + 1))
  }

  val int: Random[Int] = {
    rng => rng.nextInt
  }

  val long: Random[Long] = {
    rng => rng.nextLong
  }

  /** The map action transforms the output of an `Random` action without modifying the state itself. */
  def map[A, B](s: Random[A])(f: A => B): Random[B] = {
    flatMap(s)(a => unit(f(a)))
  }

  /** This function takes two `Random` actions, `ra` and `rb`, and a function `f` for combining their results, and
    * returns a new `Random` action that combines them.
    */
  def map2[A, B, C](ra: Random[A], rb: Random[B])(f: (A, B) => C): Random[C] = {
    flatMap(ra)(a => map(rb)(b => f(a, b)))
  }

  def flatMap[A, B](f: Random[A])(g: A => Random[B]): Random[B] = {
    rng => val (a, rng1) = f(rng); g(a)(rng1)
  }

  /** The unit action passes the `RandomNumberGenerator` state through without using it, always returning a constant
    * value rather than a random value.
    */
  def unit[A](a: A): Random[A] = {
    rng => (a, rng)
  }

  def nonNegativeInt: Random[Int] = {
    rng => val (i, rng1) = rng.nextInt; (if (i < 0) -(i + 1) else i, rng1)
  }

  def nonNegativeLong: Random[Long] = {
    rng => val (i, rng1) = rng.nextLong; (if (i < 0) -(i + 1) else i, rng1)
  }

  def both[A, B](ra: Random[A], rb: Random[B]): Random[(A, B)] = {
    map2(ra, rb)((a, b) => (a, b))
  }

  def sequence[A](fs: List[Random[A]]): Random[List[A]] = {
    fs.foldRight(unit(List.empty[A]))((f, result) => map2(f, result)((h, t) => h :: t))
  }

  /** Generates an integer between 0 (inclusive) and n (exclusive) */
  def nonNegativeLessThan(n: Int): Random[Int] = {
    flatMap(nonNegativeInt)(i => if (i + (n - 1) - (i % n) >= 0) unit(i % n) else nonNegativeLessThan(n))
  }

  /** Generates a long integer between 0 (inclusive) and n (exclusive) */
  def nonNegativeLessThan(n: Long): Random[Long] = {
    flatMap(nonNegativeLong)(i => if (i + (n - 1) - (i % n) >= 0) unit(i % n) else nonNegativeLessThan(n))
  }

  def nonNegativeMultipleOf(n: Int): Random[Int] = {
    flatMap(nonNegativeInt)(i => if (i % n == 0) unit(i) else nonNegativeMultipleOf(n))
  }

  def nonNegativeMultipleOf(n: Long): Random[Long] = {
    flatMap(nonNegativeInt)(i => if (i % n == 0) unit(i) else nonNegativeMultipleOf(n))
  }

  def positiveLessThan(n: Int): Random[Int] = {
    map(nonNegativeLessThan(n))(i => i + 1)
  }

  def positiveLessThan(n: Long): Random[Long] = {
    map(nonNegativeLessThan(n))(i => i + 1)
  }

  def positiveMultipleOf(n: Long): Random[Long] = {
    map(nonNegativeMultipleOf(n))(i => i + n)
  }

}