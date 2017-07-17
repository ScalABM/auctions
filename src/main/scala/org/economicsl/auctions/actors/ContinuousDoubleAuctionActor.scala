package org.economicsl.auctions.actors

import akka.actor.{ActorIdentity, ActorRef, DiagnosticActorLogging, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import org.economicsl.auctions.quotes.{AskPriceQuoteRequest, BidPriceQuoteRequest, SpreadQuoteRequest}
import org.economicsl.auctions.singleunit.orders.{AskOrder, BidOrder, Order}
import org.economicsl.auctions.singleunit.pricing.PricingPolicy
import org.economicsl.auctions.singleunit.twosided.OpenBidDoubleAuction
import org.economicsl.core.{Quantity, Tradable}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


/**
  *
  * @param pricingPolicy
  * @param tickSize
  * @param tradable
  * @param auctionRegistryTimeout
  * @param auctionRegistryPath
  * @param settlementServiceTimeout
  * @param settlementServicePath
  * @tparam T
  *
  * If auction is unable to initially acquire an auctionRegistry and the settlementService, then it should shutdown.
  *
  * Auction should shutdown in the event that all auction participants have de-registered.
  */
class ContinuousDoubleAuctionActor[T <: Tradable] private(pricingPolicy: PricingPolicy[T],
                                                          tickSize: Long,
                                                          tradable: T,
                                                          val auctionRegistryTimeout: Duration,
                                                          val auctionRegistryPath: String,
                                                          val settlementServiceTimeout: Duration,
                                                          val settlementServicePath: String)
    extends StackableActor
    with DiagnosticActorLogging
    with Timestamper
    with ReferenceProvider {

  /* Imports all relevant messages that need to be handled by this actor. */
  import AuctionParticipant._
  import ContinuousDoubleAuctionActor._
  import OrderTracking._

  wrappedBecome(identifyingAuxiliaryServices)

  /** Identify the auxiliary service providers.
    *
    * @return
    * @note the order in which these the messages can be received is arbitrary.
    */
  def identifyingAuxiliaryServices: Receive = {
    case message @ ActorIdentity("auctionRegistry", Some(actorRef)) =>
      log.info("Acquired auctionRegistry!")
      super.receive(message)
      actorRef ! RegisterAuction(self)
      context.become(identifyingSettlementService(actorRef))
    case message @ ActorIdentity("auctionRegistry", None) =>
      super.receive(message)
    case message @ ActorIdentity("settlementService", Some(actorRef)) =>
      log.info("Acquired settlementService!")
      super.receive(message)
      context.become(identifyingAuctionRegistry(actorRef))
    case message @ ActorIdentity("settlementService", None) =>
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /** Identify the settlement service provider given the auction registration service provider.
    *
    * @param auctionRegistry the `ActorRef` of the auction registration service provider.
    * @return
    */
  def identifyingSettlementService(auctionRegistry: ActorRef): Receive = {
    case message @ ActorIdentity("settlementService", Some(actorRef)) =>
      super.receive(message)
      context.become(active(auctionRegistry, actorRef))
    case message @ ActorIdentity("settlementService", None) =>
      super.receive(message)
    case RegisterAuctionParticipant(actorRef) =>
      context.watch(actorRef)
      ticker = ticker.addRoutee(actorRef)
      actorRef ! AuctionProtocol(tickSize, tradable)
    case Terminated(actorRef) =>
      context.unwatch(actorRef)
      ticker = ticker.removeRoutee(actorRef)
    case message =>
      super.receive(message)
  }

  /** Identify the auction registration service provider given the settlement service provider.
    *
    * @param settlementService the `ActorRef` of the settlement service provider.
    * @return
    */
  def identifyingAuctionRegistry(settlementService: ActorRef): Receive = {
    case message @ ActorIdentity("auctionRegistry", Some(actorRef)) =>
      super.receive(message)
      actorRef ! RegisterAuction(self)
      context.become(active(actorRef, settlementService))
    case message @ ActorIdentity("auctionRegistry", None) =>
      super.receive(message)
    case message =>
      super.receive(message)
  }

  /**
    *
    * @return
    * @todo
    */
  def registeringAuctionParticipants: Receive = {
    case RegisterAuctionParticipant(participant) =>
      context.watch(participant)
      participant ! AuctionProtocol(tickSize, tradable)
      ticker = ticker.addRoutee(participant)
    case DeregisterAuctionParticipant(participant) =>
      context.unwatch(participant)
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
    case Terminated(participant) if participants.contains(participant) =>
      context.unwatch(participant)
      participants = participants - participant
      ticker = ticker.removeRoutee(participant)
  }

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

  def handleQuoteRequest: Receive = {
    case quote: AskPriceQuoteRequest[T] =>
      sender() ! auction.receive(quote)
    case quote: BidPriceQuoteRequest[T] =>
      sender() ! auction.receive(quote)
    case quote: SpreadQuoteRequest[T] =>
      sender() ! auction.receive(quote)
  }

  def active(registrationService: ActorRef, settlementService: ActorRef): Receive = {
    processOrders(settlementService) orElse handleQuoteRequest orElse registeringAuctionParticipants
  }

  private[this] var auction = OpenBidDoubleAuction.withUniformPricing(pricingPolicy, tickSize)

  private[this] var participants = Set.empty[ActorRef]

  /* Router will broadcast messages to all registered auction participants (even if participants are remote!) */
  protected var ticker: Router = Router(BroadcastRoutingLogic(), Vector.empty[ActorRefRoutee])

}


object ContinuousDoubleAuctionActor {

  def props[T <: Tradable](pricingPolicy: PricingPolicy[T],
                           tickSize: Long,
                           tradable: T,
                           auctionRegistryTimeout: Duration,
                           auctionRegistryPath: String,
                           settlementServiceTimeout: Duration,
                           settlementServicePath: String): Props = {
    Props(
      new ContinuousDoubleAuctionActor(
        pricingPolicy,
        tickSize,
        tradable,
        auctionRegistryTimeout,
        auctionRegistryPath,
        settlementServiceTimeout,
        settlementServicePath
      )
    )
  }

  /**
    *
    * @param token the unique (to the auction participant) identifier of the order.
    * @param order
    * @tparam T
    */
  final case class InsertOrder[T <: Tradable](token: Token, order: Order[T])


  /** The cancel order message is used to request that an order be canceled or reduced.
    *
    * @param reference the unique (to the auction) identifier of the previously accepted order.
    * @param intended `Some(quantity)` if the order is reduced; otherwise `None` if the order is canceled.
    * @note the "intended" quantity is the maximum quantity that can be executed in total after the cancel is applied.
    * @author davidrpugh
    * @since 0.2.0
    */
  final case class CancelOrder(reference: Reference, intended: Option[Quantity])

}
