package org.pingel.hammer

import scala.concurrent.Future

trait LoadGenerator {
  def makeNextRequest(): Future[String]
}
