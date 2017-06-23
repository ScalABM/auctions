package org.economicsl.auctions.actors

import java.util.UUID

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, ReceiveTimeout, Terminated}
import org.economicsl.auctions.singleunit.orders.Order
import org.economicsl.core.{Currency, Quantity, Tradable}

import scala.concurrent.duration._


/** Base trait for all `AuctionParticipant` actors.
  *
  * @author davidrpugh
  * @since 0.2.0
  */
trait AuctionParticipantActor
    extends Actor
    with ActorLogging
    with AuctionRegistryProvider
    with UUIDProvider {

  import AuctionParticipantActor._

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    identify(auctionRegistryPath, "auctionRegistry")
  }

  def reIdentifyAuctionRegistry: Receive = {
    case Terminated(actorRef) if auctionRegistry.contains(actorRef) =>
      context.unwatch(actorRef)
      identify(auctionRegistryPath, "auctionRegistry")
  }

  def registerAuction: Receive = {
    case RegisterAuction(auctionRefs) =>
      auctionRefs.foreach{ auctionRef =>
        context.watch(auctionRef)
        auctionRef ! RegisterAuctionParticipant(self)
      }
    case Terminated(actorRef) if auctions.contains(actorRef) =>
      context.unwatch(actorRef)
      auctions = auctions - actorRef
    case protocol : AuctionProtocol =>
      auctions = auctions + (sender() -> protocol)
  }

  def orderTracking: Receive = {
    case Accepted(_, token, order, reference) =>
      outstandingOrders = outstandingOrders + (token -> (reference, order))
    case order: Rejected =>
      log.warning(order.toString)
    case Canceled(_, token, _, _) =>
      outstandingOrders = outstandingOrders - token  // todo: with multi-unit orders will need to use decrement!
  }

  def active: Receive = {
    registerAuction orElse orderTracking
  }

  /* An `AuctionParticipant` needs to keep track of multiple auction protocols. */
  protected var auctions: Map[AuctionRef, AuctionProtocol] = Map.empty

  /* An `AuctionParticipant` needs to keep track of outstanding orders. */
  protected var outstandingOrders: Map[Token, (Reference, Order[Tradable])] = Map.empty

}


object AuctionParticipantActor {

  /** Message used to indicate that a previously submitted order was accepted.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param order the previously submitted order that has been accepted.
    * @param reference A unique (to the auction) reference number assigned to the order at the time of receipt.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Accepted(timestamp: Long, token: Token, order: Order[Tradable], reference: UUID)


  /** Message used to indicate that a previously submitted order has been rejected.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Rejected(timestamp: Long, token: Token, reason: Throwable)


  /** Message used to indicate that a previously accepted order has been canceled or reduced.
    *
    * @param timestamp
    * @param token the unique (to the auction participant) identifier of the previously accepted order.
    * @param decrement the quantity of the `Tradable` just decremented from the order.
    * @param reason
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class Canceled(timestamp: Long, token: Token, decrement: Quantity, reason: String)


  /** Need some data structure to convey the information about an auction to participants. */
  final case class AuctionProtocol(tickSize: Currency, tradable: Tradable)

}
