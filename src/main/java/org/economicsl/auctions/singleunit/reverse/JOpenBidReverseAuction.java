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
package org.economicsl.auctions.singleunit.reverse;


import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.quotes.BidPriceQuote;
import org.economicsl.auctions.quotes.BidPriceQuoteRequest;
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.Fill;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing an open-bid, reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidReverseAuction<T extends Tradable>
        extends AbstractOpenBidReverseAuction<T, JOpenBidReverseAuction<T>> {

    public JOpenBidReverseAuction(BidOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.auction = OpenBidReverseAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    public JOpenBidReverseAuction<T> insert(AskOrder<T> order) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JOpenBidReverseAuction<>(ops.insert(order));
    }

    public Option<BidPriceQuote> receive(BidPriceQuoteRequest request) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    public JOpenBidReverseAuction<T> remove(AskOrder<T> order) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JOpenBidReverseAuction<>(ops.remove(order));
    }

    public JClearResult<T, JOpenBidReverseAuction<T>> clear() {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidReverseAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));
        return new JClearResult<>(fills, new JOpenBidReverseAuction<>(results.residual()));
    }

    private OpenBidReverseAuction<T> auction;

    private JOpenBidReverseAuction(OpenBidReverseAuction<T> a) {
        this.auction = a;
    }

    private OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> mkReverseAuctionLikeOps(OpenBidReverseAuction<T> a) {
        return OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(a);
    }

}
