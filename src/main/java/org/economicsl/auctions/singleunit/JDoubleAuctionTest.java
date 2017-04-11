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

import org.economicsl.auctions.singleunit.pricing.WeightedAveragePricingRule;

import java.util.Optional;
import java.util.UUID;

public class JDoubleAuctionTest {

    public static void main( String[] args ) {
        JDoubleAuction<Service> auction = JDoubleAuction.withUniformPricing(
                LimitAskOrder$.MODULE$.<LimitAskOrder<Service>>ordering(),
                LimitBidOrder$.MODULE$.<LimitBidOrder<Service>>ordering().reverse());

        LimitBidOrder<Service> bid1 = LimitBidOrder$.MODULE$.apply(UUID.randomUUID(), 10, new Service(1));
        LimitAskOrder<Service> ask1 = LimitAskOrder$.MODULE$.apply(UUID.randomUUID(), 5, new Service(1));
        auction = auction.insert(bid1);
        auction = auction.insert(ask1);
        LimitBidOrder<Service> bid2 = LimitBidOrder$.MODULE$.apply(UUID.randomUUID(), 3, new Service(1));
        LimitAskOrder<Service> ask2 = LimitAskOrder$.MODULE$.apply(UUID.randomUUID(), 12, new Service(1));
        auction = auction.insert(bid2);
        auction = auction.insert(ask2);
        Optional<JDoubleAuction<Service>.ClearResult<Service>> clear = auction.clear(new WeightedAveragePricingRule<Service>(1.0));
        if(clear.isPresent()) {
            JDoubleAuction<Service>.ClearResult<Service> result = clear.get();
            auction = result.getAuction();
            result.getFills().forEach(fill -> System.out.println(fill));
        }
    }
}
