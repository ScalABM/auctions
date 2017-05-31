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
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing a second-price, open-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSecondPriceOpenBidAuction<T extends Tradable>
        extends AbstractOpenBidAuction<T, JSecondPriceOpenBidAuction<T>> {

    public JSecondPriceOpenBidAuction(AskOrder<T> reservation) {
        this.auction = OpenBidAuction$.MODULE$.apply(reservation, new BidQuotePricingPolicy<T>());
    }

    /** Create a new instance of `JSecondPriceOpenBidAuction` whose order book contains an additional `BidOrder`.
     *
     * @param order the `BidOrder` that should be added to the `orderBook`.
     * @return an instance of `JSecondPriceOpenBidOrder` whose order book contains all previously submitted `BidOrder`
     * instances.
     */
    public JSecondPriceOpenBidAuction<T> insert(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return new JSecondPriceOpenBidAuction<>(ops.insert(order));
    }

    public Option<AskPriceQuote> receive(AskPriceQuoteRequest<T> request) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    public JSecondPriceOpenBidAuction<T> remove(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return new JSecondPriceOpenBidAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<T, JSecondPriceOpenBidAuction<T>> clear() {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JSecondPriceOpenBidAuction<>(results.residual()));
    }

    private OpenBidAuction<T> auction;

    private JSecondPriceOpenBidAuction(OpenBidAuction<T> a) {
        this.auction = a;
    }

    private OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> mkAuctionLikeOps(OpenBidAuction<T> a) {
        return OpenBidAuction$.MODULE$.openAuctionLikeOps(a);
    }

}
