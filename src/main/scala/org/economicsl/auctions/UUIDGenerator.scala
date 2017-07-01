package org.economicsl.auctions

import java.util.UUID


sealed trait UUIDGenerator


trait ReferenceGenerator
    extends UUIDGenerator {

  def randomReference(): Reference = {
    UUID.randomUUID()
  }

}


trait TokenGenerator
  extends UUIDGenerator {

  def randomToken(): Token = {
    UUID.randomUUID()
  }

}