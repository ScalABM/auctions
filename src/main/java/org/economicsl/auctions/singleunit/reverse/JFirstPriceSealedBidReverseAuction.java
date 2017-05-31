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
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.Fill;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingPolicy;
import scala.Option;

import java.util.stream.Stream;


/** Class implementing a first-price, sealed-bid reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JFirstPriceSealedBidReverseAuction<T extends Tradable>
        extends AbstractSealedBidReverseAuction<T, JFirstPriceSealedBidReverseAuction<T>> {

    public JFirstPriceSealedBidReverseAuction(BidOrder<T> reservation) {
        this.auction = SealedBidReverseAuction$.MODULE$.apply(reservation, new BidQuotePricingPolicy<T>());
    }

    /** Create a new instance of `JFirstPriceSealedBidReverseAuction` whose order book contains an additional `AskOrder`.
     *
     * @param order the `AskOrder` that should be added to the `orderBook`.
     * @return an instance of `JFirstPriceSealedBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances.
     */
    public JFirstPriceSealedBidReverseAuction<T> insert(AskOrder<T> order) {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JFirstPriceSealedBidReverseAuction<>(ops.insert(order));
    }

    /** Create a new instance of `JFirstPriceSealedBidReverseAuction` whose order book contains all previously submitted
     * `AskOrder` instances except the `order`.
     *
     * @param order the `AskOrder` that should be added to the order Book.
     * @return an instance of type `JFirstPriceSealedBidReverseAuction` whose order book contains all previously
     * submitted `AskOrder` instances except the `order`.
     */
    public JFirstPriceSealedBidReverseAuction<T> remove(AskOrder<T> order) {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        return new JFirstPriceSealedBidReverseAuction<>(ops.remove(order));
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public JClearResult<T, JFirstPriceSealedBidReverseAuction<T>> clear() {
        SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = mkReverseAuctionLikeOps(this.auction);
        ClearResult<T, SealedBidReverseAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> toJavaStream(f, false));  // todo consider parallel=true
        return new JClearResult<>(fills, new JFirstPriceSealedBidReverseAuction<>(results.residual()));
    }

    private SealedBidReverseAuction<T> auction;

    private JFirstPriceSealedBidReverseAuction(SealedBidReverseAuction<T> a) {
        this.auction = a;
    }

    private SealedBidReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> mkReverseAuctionLikeOps(SealedBidReverseAuction<T> a) {
        return SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(a);
    }

}
