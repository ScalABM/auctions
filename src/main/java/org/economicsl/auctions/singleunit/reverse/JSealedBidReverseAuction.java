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
import org.economicsl.auctions.singleunit.AuctionLike;
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.SealedBidAuction;
import org.economicsl.auctions.singleunit.SealedBidAuction$;
import org.economicsl.auctions.singleunit.orders.AskOrder;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;


/** Class implementing a sealed-bid auction.
 *
 * @param <T>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JSealedBidReverseAuction<T extends Tradable> {

    private SealedBidReverseAuction<T> auction;

    public JSealedBidReverseAuction(BidOrder<T> reservation, PricingPolicy<T> pricingPolicy) {
        this.auction = SealedBidReverseAuction$.MODULE$.apply(reservation, pricingPolicy);
    }

    public JSealedBidReverseAuction<T> insert(AskOrder<T> order) {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.auction);
        return new JSealedBidReverseAuction<>(ops.insert(order));
    }

    public JSealedBidReverseAuction<T> remove(AskOrder<T> order) {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.auction);
        return new JSealedBidReverseAuction<>(ops.remove(order));
    }

    public ClearResult<T, JSealedBidReverseAuction<T>> clear() {
        ReverseAuctionLike.Ops<T, SealedBidReverseAuction<T>> ops = SealedBidReverseAuction$.MODULE$.reverseAuctionLikeOps(this.auction);
        ClearResult<T, SealedBidReverseAuction<T>> results = ops.clear();
        return new ClearResult<>(results.fills(), new JSealedBidReverseAuction<>(results.residual()));
    }

    private JSealedBidReverseAuction(SealedBidReverseAuction<T> a) {
      this.auction = a;
    }

}
