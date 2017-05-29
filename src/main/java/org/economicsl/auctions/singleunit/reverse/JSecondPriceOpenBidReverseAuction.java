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
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingPolicy;
import scala.Option;
import scala.collection.JavaConverters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Class implementing a second-price, open-bid reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSecondPriceOpenBidReverseAuction<T extends Tradable> {

    private OpenBidReverseAuction<T> auction;

    public JSecondPriceOpenBidReverseAuction(BidOrder<T> reservation) {
        this.auction = OpenBidReverseAuction$.MODULE$.apply(reservation, new AskQuotePricingPolicy());
    }

    public JSecondPriceOpenBidReverseAuction<T> insert(AskOrder<T> order) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(this.auction);
        return new JSecondPriceOpenBidReverseAuction<>(ops.insert(order));
    }

    public Option<BidPriceQuote> receive(BidPriceQuoteRequest request) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    public JSecondPriceOpenBidReverseAuction<T> remove(AskOrder<T> order) {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(this.auction);
        return new JSecondPriceOpenBidReverseAuction<>(ops.remove(order));
    }

    public JClearResult<T, JSecondPriceOpenBidReverseAuction<T>> clear() {
        OpenReverseAuctionLike.Ops<T, OpenBidReverseAuction<T>> ops = OpenBidReverseAuction$.MODULE$.openReverseAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidReverseAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> StreamSupport.stream(JavaConverters.asJavaIterable(f).spliterator(), false));
        return new JClearResult<>(fills, new JSecondPriceOpenBidReverseAuction<>(results.residual()));
    }

    private JSecondPriceOpenBidReverseAuction(OpenBidReverseAuction<T> a) { this.auction = a; }

}
