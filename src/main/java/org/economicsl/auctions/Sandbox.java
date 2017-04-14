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

package org.economicsl.auctions;

import org.economicsl.auctions.singleunit.*;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.pricing.AskQuotePricingRule;
import org.economicsl.auctions.singleunit.pricing.BidQuotePricingRule;
import org.economicsl.auctions.singleunit.pricing.MidPointPricingRule;
import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingRule;
import scala.Option;

import java.util.Optional;
import java.util.UUID;

public class Sandbox {

    public static void main(String[] args) {

        UUID issuer = UUID.randomUUID();
        GoogleStock google = new GoogleStock(1);

        org.economicsl.auctions.multiunit.LimitBidOrder<GoogleStock> order1 = new org.economicsl.auctions.multiunit.LimitBidOrder<>(issuer, 10, 100, google);

        // Create a multi-unit market ask order
        org.economicsl.auctions.multiunit.MarketAskOrder<GoogleStock> order2 = new org.economicsl.auctions.multiunit.MarketAskOrder<>(issuer, 100, google);

        // Create some single-unit limit ask orders...
        LimitAskOrder<GoogleStock> order3 = new LimitAskOrder<>(issuer, 5, google);
        LimitAskOrder<GoogleStock> order4 = new LimitAskOrder<>(issuer, 6, google);

        // Create a multi-unit limit bid order...
        org.economicsl.auctions.multiunit.LimitBidOrder<GoogleStock> order5 = new org.economicsl.auctions.multiunit.LimitBidOrder<>(issuer, 10, 100, google);

        // Create a multi-unit market bid order...
        org.economicsl.auctions.multiunit.MarketBidOrder<GoogleStock> order7 = new org.economicsl.auctions.multiunit.MarketBidOrder<>(issuer, 100, google);

        // Create some single-unit limit bid orders...
        LimitBidOrder<GoogleStock> order8 = new LimitBidOrder<>(issuer, 10, google);
        LimitBidOrder<GoogleStock> order9 = new LimitBidOrder<>(issuer, 6, google);

        // Create an order for some other tradable
        AppleStock apple = new AppleStock(2);
        LimitBidOrder<AppleStock> order10 = new LimitBidOrder<>(issuer, 10, apple);

        // Create a four-heap order book and add some orders...
        FourHeapOrderBook<GoogleStock> orderBook1 = FourHeapOrderBook.empty(
                LimitAskOrder$.MODULE$.ordering(),
                LimitBidOrder$.MODULE$.ordering());

        FourHeapOrderBook<GoogleStock> orderBook2 = orderBook1.$plus(order3);
        FourHeapOrderBook<GoogleStock> orderBook3 = orderBook2.$plus(order4);
        FourHeapOrderBook<GoogleStock> orderBook4 = orderBook3.$plus(order9);
        FourHeapOrderBook<GoogleStock> orderBook5 = orderBook4.$plus(order8);

        // example of a uniform price auction that would be incentive compatible for the sellers...
        AskQuotePricingRule<GoogleStock> askQuotePricing = new AskQuotePricingRule<>();
        Option<Price> price1 = askQuotePricing.apply(orderBook5);
        if(price1.isDefined()) {
            System.out.println(price1.get().value());
        }

        // example of a uniform price auction that would be incentive compatible for the buyers...
        BidQuotePricingRule<GoogleStock> bidQuotePricing = new BidQuotePricingRule<GoogleStock>();
        Option<Price> price2 = bidQuotePricing.apply(orderBook5);
        if(price2.isDefined()) {
            System.out.println(price2.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        MidPointPricingRule midPointPricing = new MidPointPricingRule<GoogleStock>();
        Option<Price> midPrice = midPointPricing.apply(orderBook5);
        if(midPrice.isDefined()) {
            System.out.println(midPrice.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        WeightedAveragePricingRule<GoogleStock> averagePricing = new WeightedAveragePricingRule<GoogleStock>(0.75);
        Option<Price> averagePrice = averagePricing.apply(orderBook5);
        if(averagePrice.isDefined()) {
            System.out.println(averagePrice.get().value());
        };

        // TODO: take a look at paired orders

        // example usage of a double auction where we don't want to define the pricing rule until later...
        DoubleAuction.WithClosedOrderBook<GoogleStock> withOrderBook = DoubleAuction$.MODULE$.withClosedOrderBook(orderBook1);
        DoubleAuction.WithClosedOrderBook<GoogleStock> withOrderBook2 = withOrderBook.insert(order3);
        DoubleAuction.WithClosedOrderBook<GoogleStock> withOrderBook3 = withOrderBook2.insert(order4);
        DoubleAuction.WithClosedOrderBook<GoogleStock> withOrderBook4 = withOrderBook3.insert(order9);
        DoubleAuction.WithClosedOrderBook<GoogleStock> withOrderBook5 = withOrderBook4.insert(order8);

        Clearing<GoogleStock> clearing = new Clearing<GoogleStock>();

        // after inserting orders, now we can define the pricing rule...
        DoubleAuction<GoogleStock> auction = withOrderBook5.withUniformPricing(midPointPricing);
        Optional<Clearing<GoogleStock>.ClearResult<GoogleStock>> result = clearing.clear(auction);
        result.ifPresent(res -> {
            res.getFills().forEach(fill -> System.out.println(fill));
        });

        // ...trivial to re-run the same auction with a different pricing rule!
        DoubleAuction<GoogleStock> auction2 = withOrderBook5.withUniformPricing(askQuotePricing);
        Optional<Clearing<GoogleStock>.ClearResult<GoogleStock>> result2 = clearing.clear(auction2);
        result2.ifPresent(res -> {
            res.getFills().forEach(fill -> System.out.println(fill));
        });

        // TODO: extend with quotes
    }
}
