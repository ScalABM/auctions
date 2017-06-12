package org.economicsl.auctions

/** Package containing classes and methods for generating random data structures for auction simulations. */
package object random {

  type Random[+A] = (RandomGenerator => (A, RandomGenerator))

}
