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


import org.economicsl.auctions.quotes.*;
import org.economicsl.auctions.ClearResult;
import org.economicsl.auctions.Fill;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.OpenBidAuction;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.util.Try;

import java.util.stream.Stream;


/** Class for creating open-bid double auctions with different pricing rules.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidDoubleAuction {

    public <T extends Tradable> DiscriminatoryPricingImpl<T> withDiscriminatoryPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new DiscriminatoryPricingImpl<>(pricingPolicy, tickSize);
    }

    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new UniformPricingImpl<>(pricingPolicy, tickSize);
    }

    /** Class implementing an open-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryPricingImpl<T extends Tradable>
            extends AbstractOpenBidDoubleAuction<T, DiscriminatoryPricingImpl<T>> {

        /* underlying Scala auction contains all of the interesting logic. */
        private OpenBidAuction.DiscriminatoryPricingImpl<T> auction;

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains an additional `AskOrder`.
         *
         * @param order the `AskOrder` that should be added to the `orderBook`.
         * @return an instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances.
         */
        public Try<DiscriminatoryPricingImpl<T>> insert(AskOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new DiscriminatoryPricingImpl<>(a));
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains an additional `BidOrder`.
         *
         * @param order the `BidOrder` that should be added to the `orderBook`.
         * @return an instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances.
         */
        public Try<DiscriminatoryPricingImpl<T>> insert(BidOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new DiscriminatoryPricingImpl<>(a));
        }

        public AskPriceQuote receive(AskPriceQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public BidPriceQuote receive(BidPriceQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public SpreadQuote receive(SpreadQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         *
         * @param order the `AskOrder` that should be added to the order book.
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public DiscriminatoryPricingImpl<T> remove(AskOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.cancel(order));
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         *
         * @param order the `BidOrder` that should be added to the order book.
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         */
        public DiscriminatoryPricingImpl<T> remove(BidOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.cancel(order));
        }

        /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
         *
         * @return an instance of `JClearResult` class.
         */
        public JClearResult<DiscriminatoryPricingImpl<T>> clear() {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            ClearResult<OpenBidAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
            Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new DiscriminatoryPricingImpl<>(results.residual()));
        }

        DiscriminatoryPricingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private DiscriminatoryPricingImpl(OpenBidAuction.DiscriminatoryPricingImpl<T> a) {
            this.auction = a;
        }

        private OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.DiscriminatoryPricingImpl<T>> mkDoubleAuctionLikeOps(OpenBidAuction.DiscriminatoryPricingImpl<T> a) {
            return OpenBidAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }


    /** Class implementing an open-bid, uniform price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class UniformPricingImpl<T extends Tradable>
            extends AbstractOpenBidDoubleAuction<T, UniformPricingImpl<T>> {

        /* underlying Scala auction contains all of the interesting logic. */
        private OpenBidAuction.UniformPricingImpl<T> auction;

        /** Create a new instance of `UniformPricingImpl` whose order book contains an additional `AskOrder`.
         *
         * @param order the `AskOrder` that should be added to the `orderBook`.
         * @return an instance of `UniformPricingImpl` whose order book contains all previously submitted `AskOrder`
         * instances.
         */
        public Try<UniformPricingImpl<T>> insert(AskOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new UniformPricingImpl<>(a));
        }

        /** Create a new instance of `UniformPricingImpl` whose order book contains an additional `BidOrder`.
         *
         * @param order the `BidOrder` that should be added to the `orderBook`.
         * @return an instance of `UniformPricingImpl` whose order book contains all previously submitted `BidOrder`
         * instances.
         */
        public Try<UniformPricingImpl<T>> insert(BidOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new UniformPricingImpl<>(a));
        }

        public AskPriceQuote receive(AskPriceQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public BidPriceQuote receive(BidPriceQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        public SpreadQuote receive(SpreadQuoteRequest<T> request) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return ops.receive(request);
        }

        /** Create a new instance of `UniformPricingImpl` whose order book contains all previously submitted `AskOrder`
         * instances except the `order`.
         *
         * @param order the `AskOrder` that should be added to the order Book.
         * @return an instance of type `UniformPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public UniformPricingImpl<T> remove(AskOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.cancel(order));
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         *
         * @param order the `BidOrder` that should be added to the order book.
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         */
        public UniformPricingImpl<T> remove(BidOrder<T> order) {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.cancel(order));
        }

        public JClearResult<UniformPricingImpl<T>> clear() {
            OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            ClearResult<OpenBidAuction.UniformPricingImpl<T>> results = ops.clear();
            Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new UniformPricingImpl<>(results.residual()));
        }

        UniformPricingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private UniformPricingImpl(OpenBidAuction.UniformPricingImpl<T> a) {
            this.auction = a;
        }

        private OpenBidDoubleAuctionLike.Ops<T, OpenBidAuction.UniformPricingImpl<T>> mkDoubleAuctionLikeOps(OpenBidAuction.UniformPricingImpl<T> a) {
            return OpenBidAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }

}
