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

import org.economicsl.auctions.quotes.AskPriceQuoteRequest;
import org.economicsl.auctions.quotes.BidPriceQuoteRequest;
import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.Fill;
import org.economicsl.auctions.singleunit.JClearResult;
import org.economicsl.auctions.singleunit.JSealedBidAuction;
import org.economicsl.auctions.singleunit.orders.LimitAskOrder;
import org.economicsl.auctions.singleunit.orders.LimitBidOrder;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.pricing.*;
import org.economicsl.auctions.singleunit.twosided.*;
import scala.Option;
import scala.collection.JavaConverters;

import java.util.UUID;

public class Sandbox {

    public static void main(String[] args) {

        UUID issuer = UUID.randomUUID();
        GoogleStock google = new GoogleStock(1);

        // Create some single-unit limit ask orders...
        LimitAskOrder<GoogleStock> order3 = new LimitAskOrder<>(issuer, 5, google);
        LimitAskOrder<GoogleStock> order4 = new LimitAskOrder<>(issuer, 6, google);

        // Create some single-unit limit bid orders...
        LimitBidOrder<GoogleStock> order8 = new LimitBidOrder<>(issuer, 10, google);
        LimitBidOrder<GoogleStock> order9 = new LimitBidOrder<>(issuer, 6, google);

        // Create an order for some other tradable
        AppleStock apple = new AppleStock(2);
        LimitBidOrder<AppleStock> order10 = new LimitBidOrder<>(issuer, 10, apple);

        // Create a four-heap order book and add some orders...
        FourHeapOrderBook<GoogleStock> orderBook1 = FourHeapOrderBook.empty();

        FourHeapOrderBook<GoogleStock> orderBook2 = orderBook1.insert(order3);
        FourHeapOrderBook<GoogleStock> orderBook3 = orderBook2.insert(order4);
        FourHeapOrderBook<GoogleStock> orderBook4 = orderBook3.insert(order9);
        FourHeapOrderBook<GoogleStock> orderBook5 = orderBook4.insert(order8);

        // example of a uniform price auction that would be incentive compatible for the sellers...
        AskQuotePricingPolicy<GoogleStock> askQuotePricing = new AskQuotePricingPolicy<>();
        Option<Price> price1 = askQuotePricing.apply(orderBook5);
        if(price1.isDefined()) {
            System.out.println(price1.get().value());
        }

        // example of a uniform price auction that would be incentive compatible for the buyers...
        BidQuotePricingPolicy<GoogleStock> bidQuotePricing = new BidQuotePricingPolicy<GoogleStock>();
        Option<Price> price2 = bidQuotePricing.apply(orderBook5);
        if(price2.isDefined()) {
            System.out.println(price2.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        MidPointPricingPolicy<GoogleStock> midPointPricing = new MidPointPricingPolicy<GoogleStock>();
        Option<Price> midPrice = midPointPricing.apply(orderBook5);
        if(midPrice.isDefined()) {
            System.out.println(midPrice.get().value());
        }

        // example of a uniform price auction that puts more weight on the bidPriceQuote and yield higher surplus for sellers
        WeightedAveragePricingPolicy<GoogleStock> averagePricing = new WeightedAveragePricingPolicy<GoogleStock>(0.75);
        Option<Price> averagePrice = averagePricing.apply(orderBook5);
        if(averagePrice.isDefined()) {
            System.out.println(averagePrice.get().value());
        };

        // try using the new Java API?
        JSealedBidAuction<GoogleStock> fbsba = new JSealedBidAuction<>(order3, askQuotePricing);
        JSealedBidAuction<GoogleStock> fpsba2 = fbsba.insert(order8);
        JSealedBidAuction<GoogleStock> fpsba3 = fpsba2.insert(order9);
        JClearResult<GoogleStock, JSealedBidAuction<GoogleStock>> results = fpsba3.clear();
        System.out.println(results.getFills().get());  // TODO: is Stream the best collection to use here?

        JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock> da = new JOpenBidDoubleAuction().withDiscriminatoryPricing(midPointPricing);
        JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock> da2 = da.insert(order3);
        JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock> da3 = da2.insert(order4);
        JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock> da4 = da3.insert(order8);

        System.out.println(da4.receive(new AskPriceQuoteRequest()));
        System.out.println(da4.receive(new BidPriceQuoteRequest()));

        JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock> da5 = da4.insert(order9);
        ClearResult<GoogleStock, JOpenBidDoubleAuction.DiscriminatoryPricingImpl<GoogleStock>> results3 = da5.clear();
        java.util.List<Fill<GoogleStock>> fills3 = JavaConverters.seqAsJavaList(results3.fills().get().toList()); // TODO: this conversion should be done inside the clear method?
        System.out.println(fills3);

        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da6 = new JOpenBidDoubleAuction().withUniformPricing(midPointPricing);
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da7 = da6.insert(order3);
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da8 = da7.insert(order4);
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da9 = da8.insert(order8);

        System.out.println(da9.receive(new AskPriceQuoteRequest()));
        System.out.println(da9.receive(new BidPriceQuoteRequest()));

        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da10 = da9.insert(order9);
        ClearResult<GoogleStock, JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock>> results4 = da10.clear();
        java.util.List<Fill<GoogleStock>> fills4 = JavaConverters.seqAsJavaList(results4.fills().get().toList()); // TODO: this conversion should be done inside the clear method?
        System.out.println(fills4);

    }
    
}
