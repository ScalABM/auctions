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
import org.economicsl.auctions.Fill;
import org.economicsl.auctions.singleunit.AuctionLike;
import org.economicsl.auctions.singleunit.AuctionParticipant.*;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.OpenBidAuction;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.orders.Order;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.auctions.singleunit.quoting.AskPriceQuoting;
import org.economicsl.auctions.singleunit.quoting.BidPriceQuoting;
import org.economicsl.auctions.singleunit.quoting.SpreadQuoting;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.Tuple2;
import scala.util.Either;
import scala.util.Try;

import java.util.UUID;
import java.util.stream.Stream;


/** Class for creating open-bid double auctions with different pricing rules.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidDoubleAuction {

    public <T extends Tradable> DiscriminatoryClearingImpl<T> withDiscriminatoryPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new DiscriminatoryClearingImpl<>(pricingPolicy, tickSize);
    }

    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(PricingPolicy<T> pricingPolicy, Long tickSize) {
        return new UniformPricingImpl<>(pricingPolicy, tickSize);
    }

    /** Class implementing an open-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryClearingImpl<T extends Tradable>
            extends AbstractDoubleAuction<T, DiscriminatoryClearingImpl<T>>
            implements AskPriceQuoting<T>, BidPriceQuoting<T>, SpreadQuoting<T> {

        /* underlying Scala auction contains all of the interesting logic. */
        private OpenBidAuction.DiscriminatoryClearingImpl<T> auction;

        /** Inserts a new `Order` into the `OpenBidDoubleAuction`.
         *
         * @param kv
         * @return
         */
        public Tuple2<DiscriminatoryClearingImpl<T>, Either<Rejected, Accepted>> insert(Tuple2<UUID, Order<T>> kv) {
            AuctionLike.Ops<T, OpenBidAuction.DiscriminatoryClearingImpl<T>> ops = mkAuctionLikeOps(this.auction);
            Tuple2<OpenBidAuction.DiscriminatoryClearingImpl<T>, Either<Rejected, Accepted>> result = ops.insert(kv);
            return new Tuple2<>(new DiscriminatoryClearingImpl<>(result._1()), result._2());
        }

        /** Create a new instance of `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         *
         * @param reference
         * @return an instance of type `DiscriminatoryPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public Tuple2<DiscriminatoryClearingImpl<T>, Option<Canceled>> cancel(UUID reference) {
            AuctionLike.Ops<T, OpenBidAuction.DiscriminatoryClearingImpl<T>> ops = mkAuctionLikeOps(this.auction);
            Tuple2<OpenBidAuction.DiscriminatoryClearingImpl<T>, Option<Canceled>> result = ops.cancel(reference);
            return new Tuple2<>(new DiscriminatoryClearingImpl<>(result._1()), result._2());
        }

        /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
         *
         * @return an instance of `JClearResult` class.
         */
        public JClearResult<DiscriminatoryClearingImpl<T>> clear() {
            AuctionLike.Ops<T, OpenBidAuction.DiscriminatoryClearingImpl<T>> ops = mkAuctionLikeOps(this.auction);
            ClearResult<OpenBidAuction.DiscriminatoryClearingImpl<T>> results = ops.clear();
            Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new DiscriminatoryClearingImpl<>(results.residual()));
        }

        DiscriminatoryClearingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private DiscriminatoryClearingImpl(OpenBidAuction.DiscriminatoryClearingImpl<T> a) {
            this.auction = a;
        }

        private AuctionLike.Ops<T, OpenBidAuction.DiscriminatoryClearingImpl<T>> mkAuctionLikeOps(OpenBidAuction.DiscriminatoryClearingImpl<T> a) {
            return OpenBidAuction.DiscriminatoryClearingImpl$.MODULE$.auctionLikeOps(a);
        }

    }


    /** Class implementing an open-bid, uniform price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class UniformPricingImpl<T extends Tradable>
            extends AbstractDoubleAuction<T,UniformPricingImpl<T>>
            implements AskPriceQuoting<T>, BidPriceQuoting<T>, SpreadQuoting<T> {

        /* underlying Scala auction contains all of the interesting logic. */
        private OpenBidAuction.UniformClearingImpl<T> auction;

        /** Create a new instance of `UniformPricingImpl` whose order book contains an additional `BidOrder`.
         *
         * @param kv
         * @return an instance of `UniformPricingImpl` whose order book contains all previously submitted `BidOrder`
         * instances.
         */
        public Tuple2<UniformPricingImpl<T>, Either<Rejected, Accepted>> insert(Tuple2<UUID, Order<T>> kv) {
            AuctionLike.Ops<T, OpenBidAuction.UniformClearingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            Tuple2<OpenBidAuction.UniformClearingImpl<T>, Either<Rejected, Accepted>> result = ops.insert(kv);
            return new Tuple2<>(new UniformPricingImpl<>(result._1()), result._2());
        }

        /** Create a new instance of `UniformPricingImpl` whose order book contains all previously submitted `AskOrder`
         * instances except the `order`.
         *
         * @param reference
         * @return an instance of type `UniformPricingImpl` whose order book contains all previously submitted
         * `AskOrder` instances except the `order`.
         */
        public Tuple2<UniformPricingImpl<T>, Option<Canceled>> cancel(UUID reference) {
            AuctionLike.Ops<T, OpenBidAuction.UniformClearingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            Tuple2<OpenBidAuction.UniformClearingImpl<T>, Option<Canceled>> result = ops.cancel(reference);
            return new Tuple2<>(new UniformPricingImpl<>(result._1()), result._2());
        }

        public JClearResult<UniformPricingImpl<T>> clear() {
            AuctionLike.Ops<T, OpenBidAuction.UniformClearingImpl<T>> ops = mkDoubleAuctionLikeOps(this.auction);
            ClearResult<OpenBidAuction.UniformClearingImpl<T>> results = ops.clear();
            Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new UniformPricingImpl<>(results.residual()));
        }

        UniformPricingImpl(PricingPolicy<T> pricingPolicy, Long tickSize) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = OpenBidAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy, tickSize);
        }

        private UniformPricingImpl(OpenBidAuction.UniformClearingImpl<T> a) {
            this.auction = a;
        }

        private AuctionLike.Ops<T, OpenBidAuction.UniformClearingImpl<T>> mkDoubleAuctionLikeOps(OpenBidAuction.UniformClearingImpl<T> a) {
            return OpenBidAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }

}
