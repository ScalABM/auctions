package org.economicsl.auctions.actors

import akka.actor.Actor
import org.economicsl.auctions.singleunit.orders.BidOrder
import org.economicsl.core.Tradable

import scala.util.{Failure, Success, Try}



trait AuctionActor[A <: { def insert(order: BidOrder[_ <: Tradable]): Try[A] }]
    extends Actor
    with Timestamper
    with ReferenceProvider {

  import AuctionActor._
  import OrderTracking._

  def receive: Receive = {
    case InsertOrder(token, order) =>
      auction.insert(order) match {
        case Success(updated) =>
          sender() ! Accepted(currentTimeMillis(), token, order, randomReference())
          auction = updated
        case Failure(ex) =>
          sender() ! Rejected(currentTimeMillis(), token, ex)
      }
    case CancelOrder(reference, _) =>
      auction.remove(reference)
  }

  def clear: Receive = {
    case ClearRequest =>
      auction
  }

  var auction: A

  def processOrders(settlementService: ActorRef): Receive = {
    case InsertOrder(token, order) => order match {
      case askOrder: AskOrder[T] =>
        auction.insert(askOrder) match {
          case Success(updated) =>
            sender() ! Accepted(currentTimeMillis(), token, order, randomUUID())
            val results = updated.clear
            results.contracts match {
              case Some(contracts) =>
                contracts.foreach(contract => settlementService ! contract)
              case None =>
                auction = results.residual
            }
          case Failure(ex) =>
            sender() ! Rejected(currentTimeMillis(), token, ex)
        }
      case bidOrder: BidOrder[T] =>
        auction.insert(bidOrder) match {
          case Success(updated) =>
            sender() ! Accepted(currentTimeMillis(), token, order, randomUUID())
            val results = updated.clear
            results.contracts match {
              case Some(contracts) =>
                contracts.foreach(contract => settlementService ! contract)
              case None =>
                auction = results.residual
            }
          case Failure(ex) =>
            sender() ! Rejected(currentTimeMillis(), token, ex)
            auction
        }
    }

  }
}


object AuctionActor {

  final case class InsertOrder[T <: Tradable](token: Token, order: BidOrder[T])

}