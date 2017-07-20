package org.economicsl.auctions

import org.economicsl.core.util.UUIDGenerator


trait TokenGenerator
  extends UUIDGenerator {

  protected def randomToken(): Token = {
    random()
  }

}
