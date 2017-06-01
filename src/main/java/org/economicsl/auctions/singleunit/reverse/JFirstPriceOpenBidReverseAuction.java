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
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy;
import scala.Option;
import scala.collection.JavaConverters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Class implementing a first-price, open-bid reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JFirstPriceOpenBidReverseAuction<T extends Tradable>
        extends AbstractOpenBidReverseAuction<T, JFirstPriceOpenBidReverseAuction<T>> {

    /* underlying Scala auction contains all of the interesting logic.*/
    private OpenBidReverseAuction<T> auction;

    public JFirstPriceOpenBidReverseAuction(BidOrder<T> reservation) {
        this.auction = OpenBidReverseAuction$.MODULE$.apply(reservation, new BidQuotePricingPolicy<T>());
    }

    /** Create a new instance of `JFirstPriceOpenBidReverseAuction` whose order book contains an additional `AskOrder`.
     *
     * @param order the `AskOrder` that should be added to the `orderBook`.
     * @return an instance of `JFirstPriceOpenBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances.
     */
    public JFirstPriceOpenBidReverseAuction<T> insert(AskOrder<T> order) {
        OpenBidReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JFirstPriceOpenBidReverseAuction<>(ops.insert(order));
    }

    public Option<BidPriceQuote> receive(BidPriceQuoteRequest<T> request) {
        OpenBidReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    /** Create a new instance of `JFirstPriceOpenBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances except the `order`.
     *
     * @param order the `AskOrder` that should be added to the order Book.
     * @return an instance of type `JFirstPriceOpenBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances except the `order`.
     */
    public JFirstPriceOpenBidReverseAuction<T> remove(AskOrder<T> order) {
        OpenBidReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JFirstPriceOpenBidReverseAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<T, JFirstPriceOpenBidReverseAuction<T>> clear() {
        OpenBidReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidReverseAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));
        return new JClearResult<>(fills, new JFirstPriceOpenBidReverseAuction<>(results.residual()));
    }

    private JFirstPriceOpenBidReverseAuction(OpenBidReverseAuction<T> a) { this.auction = a; }

    private OpenBidReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> mkReverseAuctionLikeOps(OpenBidReverseAuction<T> a) {
        return OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(a);
    }

}
