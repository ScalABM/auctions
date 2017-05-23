/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit.twosided;


import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.quotes.*;
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;


/** Class implementing a sealed-bid, discriminatory price double auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidDoubleAuction<T extends Tradable> {

    private SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;  // TODO: what about uniform pricing impl?

    public JSealedBidDoubleAuction(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
        this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
    }

    public JSealedBidDoubleAuction(PricingPolicy<T> pricingPolicy) {
        FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
        this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
    }

    public JSealedBidDoubleAuction<T> insert(AskOrder<T> order) {
        DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JSealedBidDoubleAuction<>(ops.insert(order));
    }

    public JSealedBidDoubleAuction<T> insert(BidOrder<T> order) {
        DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JSealedBidDoubleAuction<>(ops.insert(order));
    }

    public JSealedBidDoubleAuction<T> remove(AskOrder<T> order) {
        DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JSealedBidDoubleAuction<>(ops.remove(order));
    }

    public JSealedBidDoubleAuction<T> remove(BidOrder<T> order) {
        DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JSealedBidDoubleAuction<>(ops.remove(order));
    }

    public ClearResult<T, JSealedBidDoubleAuction<T>> clear() {
        DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        ClearResult<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
        return new ClearResult<>(results.fills(), new JSealedBidDoubleAuction<>(results.residual()));
    }

    private JSealedBidDoubleAuction(SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
        this.auction = a;
    }

}
