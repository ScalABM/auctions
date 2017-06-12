package org.economicsl.auctions.random


case class LinearCongruentialGenerator(seed: Long) extends RandomGenerator {

  def nextInt: (Int, RandomGenerator) = {
    val updated = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val shifted = updated >>> 16 // todo: why 16 instead of 32 or 64?
    (shifted.toInt, LinearCongruentialGenerator(shifted))
  }

  def nextLong: (Long, RandomGenerator)  = {
    val updated = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val shifted = updated >>> 16 // todo: why 16 instead of 32 or 64?
    (shifted, LinearCongruentialGenerator(shifted))
  }

}
