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
import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.singleunit.orders.BidOrder;
import scala.collection.Iterable;
import scala.collection.JavaConverters;
import scala.util.Try;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Abstract base class for all sealed-bid auction mechanisms.
 *
 * @param <T>
 * @param <A>
 */
abstract class AbstractSealedBidAuction<T extends Tradable, A> {

    /** Create a new instance of type `A` whose order book contains an additional `BidOrder`.
     *
     * @param order the `BidOrder` that should be added to the `orderBook`.
     * @return an instance of type `A` whose order book contains all previously submitted `BidOrder` instances.
     */
    public abstract Try<A> insert(BidOrder<T> order);

    /** Create a new instance of type `A` whose order book contains all previously submitted `BidOrder` instances
     * except the `order`.
     *
     * @param order the `BidOrder` that should be added to the order Book.
     * @return an instance of type `A` whose order book contains all previously submitted `BidOrder` instances except
     * the `order`.
     */
    public abstract A remove(BidOrder<T> order);

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
