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
package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.quotes.AskPriceQuote;
import org.economicsl.auctions.quotes.AskPriceQuoteRequest;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing a first-price, open-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JFirstPriceOpenBidAuction<T extends Tradable>
        extends AbstractOpenBidAuction<T, JFirstPriceOpenBidAuction<T>> {

    public JFirstPriceOpenBidAuction(AskOrder<T> reservation) {
        this.auction = OpenBidAuction$.MODULE$.apply(reservation, new AskQuotePricingPolicy<T>());
    }

    public JFirstPriceOpenBidAuction<T> insert(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JFirstPriceOpenBidAuction<>(ops.insert(order));
    }

    public Option<AskPriceQuote> receive(AskPriceQuoteRequest<T> request) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    public JFirstPriceOpenBidAuction<T> remove(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JFirstPriceOpenBidAuction<>(ops.remove(order));
    }

    public JClearResult<T, JFirstPriceOpenBidAuction<T>> clear() {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JFirstPriceOpenBidAuction<>(results.residual()));
    }

    private OpenBidAuction<T> auction;

    private JFirstPriceOpenBidAuction(OpenBidAuction<T> a) {
        this.auction = a;
    }

    private OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> mkAuctionLikeOps(OpenBidAuction<T> a) {
        return OpenBidAuction$.MODULE$.openAuctionLikeOps(a);
    }

}
