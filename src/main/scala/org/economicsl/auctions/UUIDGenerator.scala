package org.economicsl.auctions

import java.util.UUID


sealed trait UUIDGenerator


trait ReferenceGenerator
    extends UUIDGenerator {

  protected def randomReference(): Reference = {
    UUID.randomUUID()
  }

}


trait TokenGenerator
  extends UUIDGenerator {

  protected def randomToken(): Token = {
    UUID.randomUUID()
  }

}