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


import org.economicsl.auctions.ClearResult;
import org.economicsl.auctions.SpotContract;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.util.Try;

import java.util.stream.Stream;


/** Class for creating "sealed-bid" double auction mechanisms.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidDoubleAuction {

    /** Create a "sealed-bid" double auction mechanism with discriminatory pricing.
     *
     * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
     * @param <T> all `AskOrder` and `BidOrder` instances submitted to the `JSealedBidDoubleAuction` must be for the
     *           same type of `Tradable`.
     * @return a `JSealedBidDoubleAuction.DiscriminatoryPricingImpl` instance.
     */
    public <T extends Tradable> DiscriminatoryPricingImpl<T> withDiscriminatoryPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new DiscriminatoryPricingImpl<>(pricingPolicy, tickSize);
    }

    /** Create a "sealed-bid" double auction mechanism with uniform pricing.
     *
     * @param pricingPolicy a `PricingPolicy` that maps a `FourHeapOrderBook` instance to an optional `Price`.
     * @param <T> all `AskOrder` and `BidOrder` instances submitted to the `JSealedBidDoubleAuction` must be for the
     *           same type of `Tradable`.
     * @return a `JSealedBidDoubleAuction.UniformPricingImpl` instance.
     */
    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new UniformPricingImpl<>(pricingPolicy, tickSize);
    }

    /** Class implementing a sealed-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryPricingImpl<T extends Tradable>
            extends AbstractSealedBidDoubleAuction<T, DiscriminatoryPricingImpl<T>> {

        /* underlying Scala auction contains all of the interesting logic. */
        private SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;

        public Try<DiscriminatoryPricingImpl<T>> insert(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new DiscriminatoryPricingImpl<>(a));
        }

        public Try<DiscriminatoryPricingImpl<T>> insert(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new DiscriminatoryPricingImpl<>(a));
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         *
         * @param order the `AskOrder` that should be added to the order book.
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public DiscriminatoryPricingImpl<T> remove(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         *
         * @param order the `BidOrder` that should be added to the order book.
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         */
        public DiscriminatoryPricingImpl<T> remove(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
         *
         * @return an instance of `JClearResult` class.
         */
        public JClearResult<DiscriminatoryPricingImpl<T>> clear() {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            ClearResult<SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
            Option<Stream<SpotContract>> fills = results.contracts().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new DiscriminatoryPricingImpl<>(results.residual()));
        }

        DiscriminatoryPricingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private DiscriminatoryPricingImpl(SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
            this.auction = a;
        }

        private SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> mkSealedBidDoubleAuctionLikeOps(SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
            return SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }


    /** Class implementing a sealed-bid, uniform price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class UniformPricingImpl<T extends Tradable>
            extends AbstractSealedBidDoubleAuction<T, UniformPricingImpl<T>> {

        /* underlying Scala auction contains all of the interesting logic. */
        private SealedBidDoubleAuction.UniformPricingImpl<T> auction;

        public Try<UniformPricingImpl<T>> insert(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new UniformPricingImpl<>(a));
        }

        public Try<UniformPricingImpl<T>> insert(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return ops.insert(order).map(a -> new UniformPricingImpl<>(a));
        }

        /** Create a new instance of `UniformPricingImpl` whose order book contains all previously submitted `AskOrder`
         * instances except the `order`.
         *
         * @param order the `AskOrder` that should be added to the order book.
         * @return an instance of type `UniformPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public UniformPricingImpl<T> remove(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        /** Create a new instance of `UniformPricingImpl` whose order book contains all previously submitted `BidOrder`
         * instances except the `order`.
         *
         * @param order the `BidOrder` that should be added to the order book.
         * @return an instance of type `UniformPricingImpl` whose order book contains all previously submitted
         * `BidOrder` instances except the `order`.
         */
        public UniformPricingImpl<T> remove(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public JClearResult<UniformPricingImpl<T>> clear() {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            ClearResult<SealedBidDoubleAuction.UniformPricingImpl<T>> results = ops.clear();
            Option<Stream<SpotContract>> fills = results.contracts().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new UniformPricingImpl<>(results.residual()));
        }

        UniformPricingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private UniformPricingImpl(SealedBidDoubleAuction.UniformPricingImpl<T> a) {
            this.auction = a;
        }

        private SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> mkSealedBidDoubleAuctionLikeOps(SealedBidDoubleAuction.UniformPricingImpl<T> a) {
            return SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }

}
