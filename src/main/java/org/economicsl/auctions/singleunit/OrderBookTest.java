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

import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook$;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;

import java.util.List;
import java.util.UUID;

public class OrderBookTest {
    public static void main(String[] args) {
        FourHeapOrderBook<Service> orderbook = FourHeapOrderBook$.MODULE$.empty(
            LimitAskOrder$.MODULE$.<LimitAskOrder<Service>>ordering(),
            LimitBidOrder$.MODULE$.<LimitBidOrder<Service>>ordering()
        );

        Service service = new Service(1);

        LimitBidOrder<Service> bid1 = LimitBidOrder$.MODULE$.apply(UUID.randomUUID(), 5, service);
        LimitAskOrder<Service> ask1 = LimitAskOrder$.MODULE$.apply(UUID.randomUUID(), 5, service);

        orderbook = orderbook.insert(bid1);
        orderbook = orderbook.insert(ask1);

        Tuple2<Stream<Tuple2<LimitAskOrder<Service>, LimitBidOrder<Service>>>, FourHeapOrderBook<Service>> tuple = orderbook.takeAllMatched();
        List<Tuple2<LimitAskOrder<Service>, LimitBidOrder<Service>>> matchedOrders = JavaConverters.seqAsJavaList(tuple._1());
        matchedOrders.forEach(t -> System.out.println(t));
    }
}
