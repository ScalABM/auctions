package org.economicsl.auctions.random


final case class Seed(current: Long) {

  def next: Seed = {
    Seed(current * 6364136223846793005L + 1442695040888963407L)
  }

}
