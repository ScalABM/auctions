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
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import scala.Option;
import scala.collection.JavaConverters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Class implementing a sealed-bid, reverse auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JOpenBidAuction<T extends Tradable> {

    private OpenBidAuction<T> auction;

    public JOpenBidAuction(AskOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.auction = OpenBidAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    public JOpenBidAuction<T> insert(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return new JOpenBidAuction<>(ops.insert(order));
    }

    public Option<AskPriceQuote> receive(AskPriceQuoteRequest request) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return ops.receive(request);
    }

    public JOpenBidAuction<T> remove(BidOrder<T> order) {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        return new JOpenBidAuction<>(ops.remove(order));
    }

    public JClearResult<T, JOpenBidAuction<T>> clear() {
        OpenBidAuctionLike.Ops<T, OpenBidAuction<T>> ops = OpenBidAuction$.MODULE$.openAuctionLikeOps(this.auction);
        ClearResult<T, OpenBidAuction<T>> results = ops.clear();
        Option<Stream<Fill<T>>> fills = results.fills().map(f -> StreamSupport.stream(JavaConverters.asJavaIterable(f).spliterator(), false));
        return new JClearResult<>(fills, new JOpenBidAuction<>(results.residual()));
    }

    private JOpenBidAuction(OpenBidAuction<T> a) {
        this.auction = a;
    }
    
}
