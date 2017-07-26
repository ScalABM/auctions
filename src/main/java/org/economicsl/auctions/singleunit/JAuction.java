package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.singleunit.orders.SingleUnitOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.core.Tradable;
import scala.Tuple2;

import java.util.UUID;


public abstract class JAuction<T extends Tradable, A extends JAuction<T, A>> {

    /** Create a new instance of type `A` whose order book contains all previously submitted `BidOrder` instances
     * except the `order`.
     *
     * @param reference
     * @return
     */
    public abstract CancelResult<A> cancel(UUID reference);

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `ClearResult` class.
     */
    public abstract ClearResult<A> clear();

    /** Create a new instance of type `A` whose order book contains an additional `BidOrder`.
     *
     * @param order
     * @return
     * @todo get rid of Tuple2 class!
     */
    public abstract InsertResult<A> insert(Tuple2<UUID, SingleUnitOrder<T>> order);

    /** Returns an auction of type `A` with a particular pricing policy. */
    public abstract A withPricingPolicy(PricingPolicy<T> updated);

    /** Returns an auction of type `A` with a particular protocol. */
    public abstract A withProtocol(Auction.AuctionProtocol<T> protocol);

}
