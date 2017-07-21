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


import org.economicsl.auctions.SpotContract;
import scala.Option;
import scala.collection.Iterable;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;

import java.util.Spliterator;
import java.util.stream.StreamSupport;


public final class ClearResult<A> {

    private A auction;
    private Option<java.util.stream.Stream<SpotContract>> result;

    ClearResult(A auction, Option<Stream<SpotContract>> result) {
        this.auction = auction;
        this.result = result.map(stream -> toJavaStream(stream, false));
    }

    public A getAuction() {
        return auction;
    }

    public Option<java.util.stream.Stream<SpotContract>> getResult() {
        return result;
    }

    /* Converts a Scala `Iterable` to a Java `Stream`. */
    private java.util.stream.Stream<SpotContract> toJavaStream(Iterable<SpotContract> iterable, boolean parallel) {
        Spliterator<SpotContract> spliterator = JavaConverters.asJavaIterable(iterable).spliterator();
        return StreamSupport.stream(spliterator, parallel);
    }

}
