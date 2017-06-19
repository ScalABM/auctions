package org.economicsl.auctions.actors


// todo consider moving to esl-core?
trait Timestamper {

  protected def currentTimeMillis(): Long = {
    System.currentTimeMillis()
  }

  protected def currentTimeNanos(): Long = {
    System.nanoTime()
  }

}

