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

import org.economicsl.auctions.Price;
import org.economicsl.auctions.Tradable;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.pricing.PricingRule;
import org.economicsl.auctions.singleunit.quotes.PriceQuotePolicy;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Stream;
import scala.math.Ordering;

import java.util.List;
import java.util.Optional;

class JDoubleAuction<T extends Tradable> {

    private DoubleAuction<T> auction = null;

    public class ClearResult<T extends Tradable> {
        private JDoubleAuction<T> auction;
        private List<Fill<T>> fills;

        public ClearResult(List<Fill<T>> _fills, JDoubleAuction<T> _auction) {
            auction = _auction;
            fills = _fills;
        }

        public JDoubleAuction<T> getAuction() {
            return auction;
        }

        public List<Fill<T>> getFills() {
            return fills;
        }
    }

    private JDoubleAuction() {
    }

    private JDoubleAuction(DoubleAuction<T> _auction) {
        auction = _auction;
    }

    public JDoubleAuction<T> insert(LimitAskOrder<T> order) {
        return new JDoubleAuction<T>(auction.insert(order));
    }

    public JDoubleAuction<T> insert(LimitBidOrder<T> order) {
        return new JDoubleAuction<T>(auction.insert(order));
    }

    public JDoubleAuction<T> remove(LimitAskOrder<T> order) {
        return new JDoubleAuction<T>(auction.remove(order));
    }

    public JDoubleAuction<T> remove(LimitBidOrder<T> order) {
        return new JDoubleAuction<T>(auction.remove(order));
    }

    public Optional<ClearResult<T>> clear() {
        Tuple2<Option<Stream<Fill<T>>>, DoubleAuction<T>> clear = auction.clear();
        Option<Stream<Fill<T>>> streamOption = clear._1();
        if(streamOption.isDefined()) {
            List<Fill<T>> fills = JavaConverters.seqAsJavaListConverter(clear._1().get()).asJava();
            JDoubleAuction<T> newAuction = new JDoubleAuction<T>(clear._2());
            return Optional.of(new ClearResult<T>(fills, newAuction));
        }
        return Optional.empty();
    }

    public static <T extends Tradable> JDoubleAuction<T> withUniformPricing(PricingRule<T, Price> p) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withUniformPricing(p));
    }

    /*public static <T extends Tradable> JDoubleAuction<T> withUniformPricing(FourHeapOrderBook<T> o, PricingRule<T, Price> p) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withUniformPricing(o,p));
    }

    public static <T extends Tradable> JDoubleAuction<T> withUniformPricing(FourHeapOrderBook<T> o, PricingRule<T, Price> p, PriceQuotePolicy<T> q) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withUniformPricing(o,p,q));
    }*/

    public static <T extends Tradable> JDoubleAuction<T> withDiscriminatoryPricing(PricingRule<T, Price> p) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withDiscriminatoryPricing(p));
    }

    /*public static <T extends Tradable> JDoubleAuction<T> withDiscriminatoryPricing(FourHeapOrderBook<T> o, PricingRule<T, Price> p) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withDiscriminatoryPricing(o,p));
    }

    public static <T extends Tradable> JDoubleAuction<T> withDiscriminatoryPricing(FourHeapOrderBook<T> o, PricingRule<T, Price> p, PriceQuotePolicy<T> q) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withDiscriminatoryPricing(o,p,q));
    }

    public static <T extends Tradable> JDoubleAuction<T> withClosedOrderBook(LimitAskOrder<T> r) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withClosedOrderBook(r));
    }

    public static <T extends Tradable> JDoubleAuction<T> withOpenOrderBook(LimitAskOrder<T> r) {
        return new JDoubleAuction<T>(DoubleAuction$.MODULE$.withOpenOrderBook(r));
    }*/

    /*private class JUniformPriceImpl<T extends Tradable> extends JDoubleAuction<T> {

        DoubleAuction$.MODULE$.UniformPricing<T> auction = null;

        public JUniformPriceImpl(DoubleAuction$.MODULE$.UniformPriceImpl<T> _auction) {
            auction = _auction;
        }

    }

    private class JDiscriminatoryPriceImpl<T extends Tradable> extends JDoubleAuction<T> {

        DoubleAuction$class.
        DoubleAuction$.MODULE$.DiscriminatoryPricing<T> auction = null;

        public JDiscriminatoryPriceImpl(DoubleAuction$.MODULE$.DiscriminatoryPricing<T> _auction) {
            auction = _auction;
        }

    }*/
}
