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
import org.economicsl.auctions.singleunit.AuctionParticipant.*;
import org.economicsl.auctions.singleunit.FirstPriceSealedBidAuction;
import org.economicsl.auctions.singleunit.OrderGenerator;
import org.economicsl.auctions.singleunit.SealedBidAuction;
import org.economicsl.auctions.singleunit.SealedBidAuction$;
import org.economicsl.auctions.singleunit.orders.LimitAskOrder;
import org.economicsl.auctions.singleunit.orders.LimitBidOrder;
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook;
import org.economicsl.auctions.singleunit.orders.Order;
import org.economicsl.auctions.singleunit.pricing.*;
import org.economicsl.auctions.singleunit.twosided.*;
import org.economicsl.core.Price;
import org.economicsl.core.Tradable;
import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.Stream;
import scala.util.Either;
import scala.util.Random;

import java.util.UUID;

public class Sandbox {

    public static void main(String[] args) {

        UUID issuer = UUID.randomUUID();
        GoogleStock google = new GoogleStock();

        // Create some single-unit limit ask orders...
        LimitAskOrder<GoogleStock> order3 = new LimitAskOrder<>(issuer, 5, google);
        LimitAskOrder<GoogleStock> order4 = new LimitAskOrder<>(issuer, 6, google);

        // Create some single-unit limit bid orders...
        LimitBidOrder<GoogleStock> order8 = new LimitBidOrder<>(issuer, 10, google);
        LimitBidOrder<GoogleStock> order9 = new LimitBidOrder<>(issuer, 6, google);

        // Create an order for some other tradable
        AppleStock apple = new AppleStock();
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
        SealedBidAuction<GoogleStock> doubleAuction = SealedBidAuction$.MODULE$.withDiscriminatoryClearingPolicy()
        Tuple2<FirstPriceSealedBidAuction<GoogleStock>, Either<Rejected, Accepted>> result = fbsba.insert(order8);
        FirstPriceSealedBidAuction<GoogleStock> fpsba3 = fpsba2.insert(order9).get();
        JClearResult<JSealedBidAuction<GoogleStock>> results = fpsba3.clear();
        System.out.println(results.getFills().get());  // TODO: is Stream the best collection to use here?

        OpenBidDoubleAuction<GoogleStock> da = new OpenBidDoubleAuction.withDiscriminatoryPricing(midPointPricing);
        JOpenBidDoubleAuction.DiscriminatoryClearingImpl<GoogleStock> da2 = da.insert(order3).get();
        JOpenBidDoubleAuction.DiscriminatoryClearingImpl<GoogleStock> da3 = da2.insert(order4).get();
        JOpenBidDoubleAuction.DiscriminatoryClearingImpl<GoogleStock> da4 = da3.insert(order8).get();

        System.out.println(da4.receive(new AskPriceQuoteRequest<>()));
        System.out.println(da4.receive(new BidPriceQuoteRequest<>()));

        JOpenBidDoubleAuction.DiscriminatoryClearingImpl<GoogleStock> da5 = da4.insert(order9).get();
        JClearResult<JOpenBidDoubleAuction.DiscriminatoryClearingImpl<GoogleStock>> results3 = da5.clear();
        System.out.println(results3.getFills().get());

        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da6 = new JOpenBidDoubleAuction().withUniformPricing(midPointPricing, 1L);
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da7 = da6.insert(order3).get();
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da8 = da7.insert(order4).get();
        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da9 = da8.insert(order8).get();

        System.out.println(da9.receive(new AskPriceQuoteRequest<>()));
        System.out.println(da9.receive(new BidPriceQuoteRequest<>()));

        JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock> da10 = da9.insert(order9).get();
        JClearResult<JOpenBidDoubleAuction.UniformPricingImpl<GoogleStock>> results4 = da10.clear();
        System.out.println(results4.getFills().get());

    }
    
}
