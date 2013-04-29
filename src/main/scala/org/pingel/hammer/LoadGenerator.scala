package org.pingel.hammer

import scala.concurrent.Future

trait LoadGenerator {
  
  def name(): String
  
  def makeNextRequest(id: Long): Future[String]
  
}
