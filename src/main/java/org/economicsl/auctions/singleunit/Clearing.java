// Copyright (c) 2017 Robert Bosch GmbH
// All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.economicsl.auctions.singleunit;

import org.economicsl.auctions.Tradable;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;

import java.util.List;
import java.util.Optional;

public class Clearing<T extends Tradable> {

    public class ClearResult<T extends Tradable> {
        private DoubleAuction<T> auction;
        private List<Fill<T>> fills;

        public ClearResult(List<Fill<T>> _fills, DoubleAuction<T> _auction) {
            auction = _auction;
            fills = _fills;
        }

        public DoubleAuction<T> getAuction() {
            return auction;
        }

        public List<Fill<T>> getFills() {
            return fills;
        }
    }

    public Optional<ClearResult<T>> clear(DoubleAuction<T> auction) {
        Tuple2<Option<Stream<Fill<T>>>, DoubleAuction<T>> clear = auction.clear();
        Option<Stream<Fill<T>>> streamOption = clear._1();
        if(streamOption.isDefined()) {
            List<Fill<T>> fills = JavaConverters.seqAsJavaListConverter(clear._1().get()).asJava();
            return Optional.of(new ClearResult<T>(fills, clear._2()));
        }
        return Optional.empty();
    }
}
