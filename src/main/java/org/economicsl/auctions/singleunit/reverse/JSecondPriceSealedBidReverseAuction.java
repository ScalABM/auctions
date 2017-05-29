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
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy;
import scala.Option;
import scala.collection.JavaConverters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Class implementing a second-price, sealed-bid reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSecondPriceSealedBidReverseAuction<T extends Tradable> {

    private SealedBidReverseAuction<T> sealedBidAuction;

    public JSecondPriceSealedBidReverseAuction(BidOrder<T> reservation) {
        this.sealedBidAuction = SealedBidReverseAuction$.MODULE$.apply(reservation, new AskQuotePricingPolicy());
    }

    public JSecondPriceSealedBidReverseAuction<T> insert(AskOrder<T> order) {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.sealedBidAuction);
        return new JSecondPriceSealedBidReverseAuction<>(ops.insert(order));
    }

    public JSecondPriceSealedBidReverseAuction<T> remove(AskOrder<T> order) {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.sealedBidAuction);
        return new JSecondPriceSealedBidReverseAuction<>(ops.remove(order));
    }

    public JClearResult<T, JSecondPriceSealedBidReverseAuction<T>> clear() {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.sealedBidAuction);
        ClearResult<T, SealedBidReverseAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> StreamSupport.stream(JavaConverters.asJavaIterable(f).spliterator(), false));
        return new JClearResult<>(fills, new JSecondPriceSealedBidReverseAuction<>(results.residual()));
    }

    private JSecondPriceSealedBidReverseAuction(SealedBidReverseAuction<T> a) {
        this.sealedBidAuction = a;
    }

}
