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


import org.economicsl.auctions.ClearResult;
import org.economicsl.auctions.Fill;
import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.quotes.AskPriceQuote;
import org.economicsl.auctions.quotes.AskPriceQuoteRequest;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy;
import scala.Option;
import scala.util.Try;

import java.util.stream.Stream;


/** Class implementing a second-price, open-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSecondPriceOpenBidAuction<T extends Tradable>
        extends AbstractOpenBidAuction<T, JSecondPriceOpenBidAuction<T>> {

    /* underlying Scala auction contains all of the interesting logic. */
    private OpenBidAuction<T> auction;

    public JSecondPriceOpenBidAuction(AskOrder<T> reservation, Long tickSize) {
        this.auction = OpenBidAuction$.MODULE$.apply(reservation, new BidQuotePricingPolicy<T>(), tickSize);
    }

    /** Create a new instance of `JSecondPriceOpenBidAuction` whose order book contains an additional `BidOrder`.
     *
     * @param order the `BidOrder` that should be added to the `orderBook`.
     * @return an instance of `JSecondPriceOpenBidOrder` whose order book contains all previously submitted `BidOrder`
     * instances.
     */
    public Try<JSecondPriceOpenBidAuction<T>> insert(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.mkAuctionOps(this.auction);
        return ops.insert(order).map(a -> new JSecondPriceOpenBidAuction<>(a));
    }

    public AskPriceQuote receive(AskPriceQuoteRequest<T> request) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.mkAuctionOps(this.auction);
        return ops.receive(request);
    }

    /** Create a new instance of `JSecondPriceOpenBidAuction` whose order book contains all previously submitted
     * `BidOrder` instances except the `order`.
     *
     * @param order the `BidOrder` that should be added to the order Book.
     * @return an instance of `JSecondPriceOpenBidAuction` whose order book contains all previously submitted `BidOrder`
     * instances except the `order`.
     */
    public JSecondPriceOpenBidAuction<T> remove(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.mkAuctionOps(this.auction);
        return new JSecondPriceOpenBidAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<JSecondPriceOpenBidAuction<T>> clear() {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<OpenBidAuction<T>> results = ops.clear();
        Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JSecondPriceOpenBidAuction<>(results.residual()));
    }

    private JSecondPriceOpenBidAuction(OpenBidAuction<T> a) {
        this.auction = a;
    }

    private OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> mkAuctionLikeOps(OpenBidAuction<T> a) {
        return OpenBidAuction$.MODULE$.mkAuctionOps(a);
    }

}
