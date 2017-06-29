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


import org.economicsl.auctions.ClearResult;
import org.economicsl.auctions.SpotContract;
import org.economicsl.auctions.singleunit.*;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.util.Try;

import java.util.stream.Stream;


/** Class implementing a sealed-bid, reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidReverseAuction<T extends Tradable>
        extends AbstractSealedBidReverseAuction<T, JSealedBidReverseAuction<T>> {

    /* underlying Scala auction contains all of the interesting logic. */
    private SealedBidReverseAuction<T> auction;

    public JSealedBidReverseAuction(BidOrder<T> reservation, PricingPolicy<T> pricingPolicy, Long tickSize) {
        this.auction = SealedBidReverseAuction$.MODULE$.apply(reservation, pricingPolicy, tickSize);
    }

    /** Create a new instance of `JSealedBidReverseAuction` whose order book contains an additional `AskOrder`.
     *
     * @param order the `AskOrder` that should be added to the `orderBook`.
     * @return an instance of `JSealedBidReverseAuction` whose order book contains all previously submitted `AskOrder`
     * instances.
     */
    public Try<JSealedBidReverseAuction<T>> insert(AskOrder<T> order) {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return ops.insert(order).map(a -> new JSealedBidReverseAuction<>(a));
    }

    /** Create a new instance of `JSealedBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances except the `order`.
     *
     * @param order the `AskOrder` that should be added to the order Book.
     * @return an instance of type `JSealedBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances except the `order`.
     */
    public JSealedBidReverseAuction<T> remove(AskOrder<T> order) {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JSealedBidReverseAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<JSealedBidReverseAuction<T>> clear() {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        ClearResult<SealedBidReverseAuction<T>> results = ops.clear();
        Option<Stream<SpotContract>> fills = results.contracts().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JSealedBidReverseAuction<>(results.residual()));
    }

    private JSealedBidReverseAuction(SealedBidReverseAuction<T> a) {
      this.auction = a;
    }

    private SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> mkReverseAuctionLikeOps(SealedBidReverseAuction<T> a) {
        return SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(a);
    }

}
