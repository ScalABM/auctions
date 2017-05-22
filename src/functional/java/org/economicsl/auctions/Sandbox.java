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

import org.economicsl.auctions.singleunit.ClearResult;
import org.economicsl.auctions.singleunit.Fill;
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

        // example usage of a double auction where we don't want to define the pricing rule until later...
        SealedBidDoubleAuction.UniformPricingImpl<GoogleStock> impl1 = SealedBidDoubleAuction$.MODULE$.withUniformPricing(midPointPricing);
        DoubleAuctionLike.Ops<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> ops = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(impl1);
        SealedBidDoubleAuction.UniformPricingImpl<GoogleStock> impl2 = ops.insert(order3);
        DoubleAuctionLike.Ops<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> ops2 = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(impl2);
        SealedBidDoubleAuction.UniformPricingImpl<GoogleStock> impl3 = ops2.insert(order4);
        DoubleAuctionLike.Ops<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> op3 = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(impl3);
        SealedBidDoubleAuction.UniformPricingImpl<GoogleStock> impl4 = op3.insert(order9);
        DoubleAuctionLike.Ops<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> ops4 = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(impl4);
        SealedBidDoubleAuction.UniformPricingImpl<GoogleStock> impl5 = ops4.insert(order8);

        // TODO: should not be able to access these from a sealed bid auction!
        System.out.println(impl5.orderBook().matchedOrders().askOrders().headOption());
        System.out.println(impl5.orderBook().matchedOrders().bidOrders().headOption());
        System.out.println(impl5.orderBook().askPriceQuote());
        System.out.println(impl5.orderBook().bidPriceQuote());

        // after inserting orders, now we can now clear the auction!
        DoubleAuctionLike.Ops<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> ops5 = SealedBidDoubleAuction.UniformPricingImpl$.MODULE$.doubleAuctionLikeOps(impl5);
        ClearResult<GoogleStock, SealedBidDoubleAuction.UniformPricingImpl<GoogleStock>> result = ops5.clear();
        java.util.List<Fill<GoogleStock>> fills = JavaConverters.seqAsJavaList(result.fills().get().toList());
        fills.forEach(System.out::println);

    }
    
}
