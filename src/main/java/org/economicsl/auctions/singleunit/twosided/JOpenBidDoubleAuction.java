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


/** Class implementing an open-bid, discriminatory price double auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidDoubleAuction<T extends Tradable> {

    private OpenBidDoubleAuction.DiscriminatoryPricingImpl<T> auction;  // TODO: what about uniform pricing impl?

    public JOpenBidDoubleAuction(FourHeapOrderBook<T> orderBook, PricingPolicy<T> pricingPolicy) {
        this.auction = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
    }

    public JOpenBidDoubleAuction(PricingPolicy<T> pricingPolicy) {
        FourHeapOrderBook<T> orderBook = FourHeapOrderBook.empty();
        this.auction = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.apply(orderBook, pricingPolicy);
    }

    public JOpenBidDoubleAuction<T> insert(AskOrder<T> order) {
        OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JOpenBidDoubleAuction<>(ops.insert(order));
    }

    public JOpenBidDoubleAuction<T> insert(BidOrder<T> order) {
        OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JOpenBidDoubleAuction<>(ops.insert(order));
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

    public JOpenBidDoubleAuction<T> remove(AskOrder<T> order) {
        OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JOpenBidDoubleAuction<>(ops.remove(order));
    }

    public JOpenBidDoubleAuction<T> remove(BidOrder<T> order) {
        OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        return new JOpenBidDoubleAuction<>(ops.remove(order));
    }

    public ClearResult<T, JOpenBidDoubleAuction<T>> clear() {
        OpenDoubleAuctionLike.Ops<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> ops = OpenBidDoubleAuction.DiscriminatoryPricingImpl$.MODULE$.doubleAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidDoubleAuction.DiscriminatoryPricingImpl<T>> results = ops.clear();
        return new ClearResult<>(results.fills(), new JOpenBidDoubleAuction<>(results.residual()));
    }

    private JOpenBidDoubleAuction(OpenBidDoubleAuction.DiscriminatoryPricingImpl<T> a) {
        this.auction = a;
    }

}
