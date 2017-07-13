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


import org.economicsl.auctions.Fill;
import org.economicsl.auctions.singleunit.AuctionParticipant.*;
import org.economicsl.auctions.singleunit.orders.Order;
import org.economicsl.auctions.singleunit.pricing.PricingPolicy;
import org.economicsl.core.Tradable;
import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.Stream;
import scala.util.Either;

import java.util.UUID;


/** Abstract base class for all sealed-bid auction mechanisms.
 *
 * @param <T>
 */
class JOpenBidAuction<T extends Tradable> extends JAuction<T, JOpenBidAuction<T>> {

    private OpenBidAuction<T> auction;

    private JOpenBidAuction(OpenBidAuction<T> auction) {
        this.auction = auction;
    }

    /** Create a new instance of type `A` whose order book contains all previously submitted `BidOrder` instances
     * except the `order`.
     *
     * @param reference
     * @return
     */
    public CancelResult<JOpenBidAuction<T>> cancel(UUID reference) {
        Tuple2<OpenBidAuction<T>, Option<Canceled>> result = auction.cancel(reference);
        JOpenBidAuction<T> jAuction = new JOpenBidAuction<>(result._1);
        return new CancelResult<>(jAuction, result._2);
    }

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `ClearResult` class.
     */
    public ClearResult<JOpenBidAuction<T>> clear() {
        Tuple2<OpenBidAuction<T>, Option<Stream<Fill>>> result = auction.clear();
        JOpenBidAuction<T> jAuction = new JOpenBidAuction<>(result._1);
        return new ClearResult<>(jAuction, result._2);
    }

    /** Create a new instance of type `A` whose order book contains an additional `BidOrder`.
     *
     * @param order
     * @return
     */
    public InsertResult<JOpenBidAuction<T>> insert(Tuple2<UUID, Order<T>> order) {
        Tuple2<OpenBidAuction<T>, Either<Rejected, Accepted>> result = auction.insert(order);
        JOpenBidAuction<T> jAuction = new JOpenBidAuction<>(result._1());
        return new InsertResult<>(jAuction, result._2());
    }

    /** Factory method for creating sealed-bid auctions with uniform clearing policy.
     *
     * @param pricingPolicy
     * @param tickSize
     * @param <T>
     * @return
     */
    public static <T extends Tradable> JOpenBidAuction<T> withUniformClearingPolicy(PricingPolicy<T> pricingPolicy, Long tickSize) {
        OpenBidAuction<T> auction = OpenBidAuction.withUniformClearingPolicy(pricingPolicy, tickSize);
        return new JOpenBidAuction<>(auction);
    }

    /** Factory method for creating sealed-bid auctons with discriminatory clearing policy.
     *
     * @param pricingPolicy
     * @param tickSize
     * @param <T>
     * @return
     */
    public static <T extends Tradable> JOpenBidAuction<T> withDiscriminatoryClearingPolicy(PricingPolicy<T> pricingPolicy, Long tickSize) {
        OpenBidAuction<T> auction = OpenBidAuction.withDiscriminatoryClearingPolicy(pricingPolicy, tickSize);
        return new JOpenBidAuction<>(auction);
    }

}