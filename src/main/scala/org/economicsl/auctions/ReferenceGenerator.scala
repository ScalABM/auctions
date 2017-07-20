package org.economicsl.auctions


import org.economicsl.core.util.UUIDGenerator


trait ReferenceGenerator
    extends UUIDGenerator {

  protected def randomReference(): Reference = {
    random()
  }

}


