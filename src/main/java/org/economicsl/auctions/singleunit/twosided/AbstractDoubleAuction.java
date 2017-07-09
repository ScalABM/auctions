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
package org.economicsl.auctions.singleunit.twosided;

import org.economicsl.auctions.Fill;
import org.economicsl.auctions.singleunit.AuctionParticipant.*;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.orders.Order;
import org.economicsl.core.Tradable;

import scala.Option;
import scala.Tuple2;
import scala.collection.Iterable;
import scala.collection.JavaConverters;
import scala.util.Either;

import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


abstract class AbstractDoubleAuction<T extends Tradable, A> {

    /** Create a new instance of type class `A` whose order book contains an additional `Order`.
     *
     * @param kv a `Tuple2` that maps a unique order identifier to an `Order` that should be added to the order book.
     *           The order identifier is used by the `AuctionParticipant` that issued the order to uniquely identifiy it
     *           within its collection of outstanding orders.
     * @return an instance `Tuple2` whose first element is some subtype of `AbstractSealedBidAuction` whose order book
     * contains all previously submitted `Order` instances and whose second element contains either `Accepted` or
     * `Rejected`, depending on whether the order was accepted into the order book or was rejected.
     */
    public abstract Tuple2<A, Either<Rejected, Accepted>> insert(Tuple2<UUID, Order<T>> kv);

    /** Create a new instance of type `A` whose order book contains all previously submitted `AskOrder` instances
     * except the `order`.
     *
     * @param reference
     * @return an instance of type `A` whose order book contains all previously submitted `AskOrder` instances except
     * the `order`.
     */
    public abstract Tuple2<A, Option<Canceled>> cancel(UUID reference);

    /** Calculate a clearing price and remove all `AskOrder` and `BidOrder` instances that are matched at that price.
     *
     * @return an instance of `JClearResult` class.
     */
    public abstract JClearResult<A> clear();

    /* Converts a Scala `Iterable` to a Java `Stream`. */
    protected Stream<Fill> toJavaStream(Iterable<Fill> input, boolean parallel) {
        return StreamSupport.stream(JavaConverters.asJavaIterable(input).spliterator(), parallel);
    }

}
