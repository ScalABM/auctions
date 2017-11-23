package org.economicsl.auctions;


import org.economicsl.auctions.messages.*;
import org.economicsl.auctions.singleunit.OpenBidAuction;
import org.economicsl.auctions.singleunit.pricing.MidPointQuotePricingPolicy;

import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.Stream;
import scala.collection.JavaConverters;
import scala.math.Ordering;
import scala.util.Either;
import scala.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ImperativePeriodicDoubleAuction {


    public static void main(String[] args) {

        // define the auction mechanism...
        TestStock googleStock = new TestStock();
        UUID auctionId = UUID.randomUUID();
        Ordering<NewSingleUnitBid<TestStock>> bidOrdering = NewSingleUnitBid.priceOrdering();
        Ordering<NewSingleUnitOffer<TestStock>> offerOrdering = NewSingleUnitOffer.priceOrdering();
        MidPointQuotePricingPolicy<TestStock> midpointQuotePricingPolicy = new MidPointQuotePricingPolicy<>();
        AuctionProtocol<TestStock> protocol = AuctionProtocol$.MODULE$.apply(googleStock);  // todo create JAuctionProtocol?
        OpenBidAuction<TestStock> doubleAuction = OpenBidAuction.withUniformClearingPolicy(auctionId, bidOrdering, offerOrdering, midpointQuotePricingPolicy, protocol);

        // generate some random order flow...
        int numberOrders = 10000;
        Random prng = new Random(42);
        Stream<Tuple2<UUID, NewSingleUnitOrder<TestStock>>> orders = NewOrderGenerator.randomSingleUnitOrders(0.5, numberOrders, googleStock, prng);

        List<Either<NewOrderRejected, NewOrderAccepted>> insertResults = new ArrayList<>();

        for (Tuple2<UUID, NewSingleUnitOrder<TestStock>> order:JavaConverters.seqAsJavaList(orders)) {
            Tuple2<OpenBidAuction<TestStock>, Either<NewOrderRejected, NewOrderAccepted>> insertResult = doubleAuction.insert(order._2);
            doubleAuction = insertResult._1();
            insertResults.add(insertResult._2());
        }

        // clear the auction...
        Tuple2<OpenBidAuction<TestStock>, Option<Stream<SpotContract>>> results = doubleAuction.clear();
        List<SpotContract> fills = JavaConverters.seqAsJavaList(results._2().get());

        // print the results to console...
        for (SpotContract fill:fills) {
            System.out.println(fill);
        }

    }

}
