package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.AuctionProtocol;
import org.economicsl.auctions.messages.CancelOrder;
import org.economicsl.auctions.messages.NewSingleUnitOrder;
import org.economicsl.auctions.singleunit.pricing.SingleUnitPricingPolicy;
import org.economicsl.core.Tradable;


public abstract class JAuction<T extends Tradable, A extends JAuction<T, A>> {

    /** Create a new instance of type `A` whose order book contains all previously submitted `BidOrder` instances
     * except the `order`.
     *
     * @param message
     * @return
     */
    public abstract CancelResult<A> cancel(CancelOrder message);

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `ClearResult` class.
     */
    public abstract ClearResult<A> clear();

    /** Create a new instance of type `A` whose order book contains an additional `BidOrder`.
     *
     * @param message
     * @return
     */
    public abstract InsertResult<A> insert(NewSingleUnitOrder<T> message);

    /** Returns an auction of type `A` with a particular pricing policy. */
    public abstract A withPricingPolicy(SingleUnitPricingPolicy<T> updated);

    /** Returns an auction of type `A` with a particular protocol. */
    public abstract A withProtocol(AuctionProtocol<T> protocol);

}
