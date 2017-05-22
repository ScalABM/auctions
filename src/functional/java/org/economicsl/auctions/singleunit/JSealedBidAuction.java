package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;


public class JSealedBidAuction<T extends Tradable> {

    private SealedBidAuction<T> sealedBidAuction;

    public JSealedBidAuction(AskOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.sealedBidAuction = SealedBidAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    public JSealedBidAuction<T> insert(BidOrder<T> order) {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = SealedBidAuction$.MODULE$.auctionLikeOps(this.sealedBidAuction);
        return new JSealedBidAuction<>(ops.insert(order));
    }

    public JSealedBidAuction<T> remove(BidOrder<T> order) {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = SealedBidAuction$.MODULE$.auctionLikeOps(this.sealedBidAuction);
        return new JSealedBidAuction<>(ops.remove(order));
    }

    public ClearResult<T, JSealedBidAuction<T>> clear() {
        AuctionLike.Ops<T, SealedBidAuction<T>> ops = SealedBidAuction$.MODULE$.auctionLikeOps(this.sealedBidAuction);
        ClearResult<T, SealedBidAuction<T>> results = ops.clear();
        return new ClearResult<>(results.fills(), new JSealedBidAuction<>(results.residual()));
    }

    private JSealedBidAuction(SealedBidAuction<T> a) {
      this.sealedBidAuction = a;
    }

}
