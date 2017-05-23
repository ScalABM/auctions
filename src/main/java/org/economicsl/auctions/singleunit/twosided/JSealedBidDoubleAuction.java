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


/** Class for creating sealed-bid double auctions.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidDoubleAuction {

    public <T extends Tradable> DiscriminatoryPricingImpl<T> withDiscriminatoryPricing(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
        return new DiscriminatoryPricingImpl<>(orderBook, pricingPolicy);
    }

    public <T extends Tradable> DiscriminatoryPricingImpl<T> withDiscriminatoryPricing(PricingPolicy<T> pricingPolicy) {
        FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
        return new DiscriminatoryPricingImpl<>(orderBook, pricingPolicy);
    }

    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
        return new UniformPricingImpl<>(orderBook, pricingPolicy);
    }

    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(PricingPolicy<T> pricingPolicy) {
        FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
        return new UniformPricingImpl<>(orderBook, pricingPolicy);
    }

    /** Class implementing a sealed-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryPricingImpl<T extends Tradable> {

        private SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;

        private DiscriminatoryPricingImpl(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
            this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public DiscriminatoryPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public DiscriminatoryPricingImpl<T> insert(AskOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public DiscriminatoryPricingImpl<T> insert(BidOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public DiscriminatoryPricingImpl<T> remove(AskOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public DiscriminatoryPricingImpl<T> remove(BidOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public ClearResult<T, DiscriminatoryPricingImpl<T>> clear() {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            ClearResult<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
            return new ClearResult<>(results.fills(), new DiscriminatoryPricingImpl<>(results.residual()));
        }

        private DiscriminatoryPricingImpl(SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
            this.auction = a;
        }


    }


    /** Class implementing a sealed-bid, uniform price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class UniformPricingImpl<T extends Tradable> {

        private SealedBidDoubleAuction.UniformPricingImpl<T> auction;

        private UniformPricingImpl(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
            this.auction = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public UniformPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public UniformPricingImpl<T> insert(AskOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public UniformPricingImpl<T> insert(BidOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public UniformPricingImpl<T> remove(AskOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public UniformPricingImpl<T> remove(BidOrder<T> order) {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public ClearResult<T, UniformPricingImpl<T>> clear() {
            DoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            ClearResult<T, SealedBidDoubleAuction.UniformPricingImpl<T>> results = ops.clear();
            return new ClearResult<>(results.fills(), new UniformPricingImpl<>(results.residual()));
        }

        private UniformPricingImpl(SealedBidDoubleAuction.UniformPricingImpl<T> a) {
            this.auction = a;
        }

    }

}
