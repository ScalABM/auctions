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
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing an open-bid, auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidAuction<T extends Tradable> extends AbstractOpenBidAuction<T, JOpenBidAuction<T>> {

    /* underlying Scala auction contains all of the interesting logic. */
    private OpenBidAuction<T> auction;

    public JOpenBidAuction(AskOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.auction = OpenBidAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    /** Create a new instance of `JOpenBidAuction` whose order book contains an additional `BidOrder`.
     *
     * @param order the `BidOrder` that should be added to the `orderBook`.
     * @return an instance of `JOpenBidOrder` whose order book contains all previously submitted `BidOrder` instances.
     */
    public JOpenBidAuction<T> insert(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JOpenBidAuction<>(ops.insert(order));
    }

    public AskPriceQuote receive(AskPriceQuoteRequest<T> request) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    /** Create a new instance of `JOpenBidAuction` whose order book contains all previously submitted `BidOrder`
     * instances except the `order`.
     *
     * @param order the `BidOrder` that should be added to the order Book.
     * @return an instance of `JOpenBidAuction` whose order book contains all previously submitted `BidOrder` instances
     * except the `order`.
     */
    public JOpenBidAuction<T> remove(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        return new JOpenBidAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<JOpenBidAuction<T>> clear() {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = mkAuctionLikeOps(this.auction);
        ClearResult<OpenBidAuction<T>> results = ops.clear();
        Option<Stream<Fill>> fills = results.fills().map(f -> toJavaStream(f, false)); // todo consider parallel=true
        return new JClearResult<>(fills, new JOpenBidAuction<>(results.residual()));
    }

    private JOpenBidAuction(OpenBidAuction<T> a) {
        this.auction = a;
    }

    private OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> mkAuctionLikeOps(OpenBidAuction<T> a) {
      return OpenBidAuction$.MODULE$.openAuctionLikeOps(a);
    }
    
}
