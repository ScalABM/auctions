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
import org.economicsl.auctions.singleunit.orders.BidOrder;
import scala.collection.Iterable;
import scala.collection.JavaConverters;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/** Abstract base class for all sealed-bid auction mechanisms.
 *
 * @param <T> all `BidOrder` instances
 */
abstract class AbstractSealedBidAuction<T extends Tradable, A> {

    public abstract A insert(BidOrder<T> order);

    public abstract A remove(BidOrder<T> order);

    public abstract JClearResult<T, A> clear();

    /* Converts a Scala `Iterable` to a Java `Stream`. */
    Stream<Fill<T>> toJavaStream(Iterable<Fill<T>> input, boolean parallel) {
        return StreamSupport.stream(JavaConverters.asJavaIterable(input).spliterator(), parallel);
    }

}
