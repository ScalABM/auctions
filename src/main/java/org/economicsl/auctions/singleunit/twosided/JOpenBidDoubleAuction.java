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


/** Class for creating open-bid double auctions with different pricing rules.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidDoubleAuction {

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

    /** Class implementing an open-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryPricingImpl<T extends Tradable> {

        private OpenBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;

        private DiscriminatoryPricingImpl(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
            this.auction = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public DiscriminatoryPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public DiscriminatoryPricingImpl<T> insert(AskOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public DiscriminatoryPricingImpl<T> insert(BidOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public Option<AskPriceQuote> receive(AskPriceQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public Option<BidPriceQuote> receive(BidPriceQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public Option<SpreadQuote> receive(SpreadQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public DiscriminatoryPricingImpl<T> remove(AskOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public DiscriminatoryPricingImpl<T> remove(BidOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public ClearResult<T, DiscriminatoryPricingImpl<T>> clear() {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            ClearResult<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
            return new ClearResult<>(results.fills(), new DiscriminatoryPricingImpl<>(results.residual()));
        }

        private DiscriminatoryPricingImpl(OpenBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
            this.auction = a;
        }


    }


    /** Class implementing an open-bid, uniform price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class UniformPricingImpl<T extends Tradable> {

        private OpenBidDoubleAuction.UniformPricingImpl<T> auction;

        private UniformPricingImpl(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
            this.auction = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public UniformPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        public UniformPricingImpl<T> insert(AskOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public UniformPricingImpl<T> insert(BidOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public Option<AskPriceQuote> receive(AskPriceQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public Option<BidPriceQuote> receive(BidPriceQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public Option<SpreadQuote> receive(SpreadQuoteRequest request) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public UniformPricingImpl<T> remove(AskOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public UniformPricingImpl<T> remove(BidOrder<T> order) {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public ClearResult<T, UniformPricingImpl<T>> clear() {
            OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.UniformPricingImpl<T>> ops = OpenBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
            ClearResult<T, OpenBidDoubleAuction.UniformPricingImpl<T>> results = ops.clear();
            return new ClearResult<>(results.fills(), new UniformPricingImpl<>(results.residual()));
        }

        private UniformPricingImpl(OpenBidDoubleAuction.UniformPricingImpl<T> a) {
            this.auction = a;
        }

    }

}
