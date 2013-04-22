package org.pingel.hammer

import scala.concurrent.Future

trait LoadGenerator {
  
  def makeNextRequest(id: Long): Future[String]
  
}
