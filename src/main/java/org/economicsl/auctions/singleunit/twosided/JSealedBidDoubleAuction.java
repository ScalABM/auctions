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
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.Fill;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class for creating sealed-bid double auctions.
 *
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidDoubleAuction {

    public <T extends Tradable> DiscriminatoryPricingImpl<T> withDiscriminatoryPricing(PricingPolicy<T> pricingPolicy) {
        return new DiscriminatoryPricingImpl<>(pricingPolicy);
    }

    public <T extends Tradable> UniformPricingImpl<T> withUniformPricing(PricingPolicy<T> pricingPolicy) {
        return new UniformPricingImpl<>(pricingPolicy);
    }

    /** Class implementing a sealed-bid, discriminatory price double auction.
     *
     * @author davidrpugh
     * @since 0.1.0
     */
    public static class DiscriminatoryPricingImpl<T extends Tradable>
            extends AbstractSealedBidDoubleAuction<T, DiscriminatoryPricingImpl<T>> {

        public DiscriminatoryPricingImpl<T> insert(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public DiscriminatoryPricingImpl<T> insert(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.insert(order));
        }

        public DiscriminatoryPricingImpl<T> remove(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public DiscriminatoryPricingImpl<T> remove(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new DiscriminatoryPricingImpl<>(ops.remove(order));
        }

        public JClearResult<T, DiscriminatoryPricingImpl<T>> clear() {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            ClearResult<T, SealedBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
            Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new DiscriminatoryPricingImpl<>(results.residual()));
        }

        DiscriminatoryPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        private SealedBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;

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

        public UniformPricingImpl<T> insert(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public UniformPricingImpl<T> insert(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.insert(order));
        }

        public UniformPricingImpl<T> remove(AskOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public UniformPricingImpl<T> remove(BidOrder<T> order) {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            return new UniformPricingImpl<>(ops.remove(order));
        }

        public JClearResult<T, UniformPricingImpl<T>> clear() {
            SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> ops = mkSealedBidDoubleAuctionLikeOps(this.auction);
            ClearResult<T, SealedBidDoubleAuction.UniformPricingImpl<T>> results = ops.clear();
            Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));
            return new JClearResult<>(fills, new UniformPricingImpl<>(results.residual()));
        }

        UniformPricingImpl(PricingPolicy<T> pricingPolicy) {
            FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
            this.auction = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
        }

        private SealedBidDoubleAuction.UniformPricingImpl<T> auction;

        private UniformPricingImpl(SealedBidDoubleAuction.UniformPricingImpl<T> a) {
            this.auction = a;
        }

        private SealedBidDoubleAuctionLike.Ops<T, SealedBidDoubleAuction.UniformPricingImpl<T>> mkSealedBidDoubleAuctionLikeOps(SealedBidDoubleAuction.UniformPricingImpl<T> a) {
            return SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(a);
        }

    }

}
