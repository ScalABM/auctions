package org.economicsl.auctions.actors

import akka.actor.Actor


trait Timestamper {
  this: Actor =>

  protected def currentTimeMillis(): Long = {
    System.currentTimeMillis()
  }

  protected def currentTimeNanos(): Long = {
    System.nanoTime()
  }

}

