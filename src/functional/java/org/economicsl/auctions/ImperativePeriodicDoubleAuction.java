package org.economicsl.auctions;


import org.economicsl.auctions.OrderTracker.*;
import org.economicsl.auctions.singleunit.OpenBidSingleUnitAuction;
import org.economicsl.auctions.singleunit.OrderGenerator;
import org.economicsl.auctions.singleunit.orders.SingleUnitOrder;
import org.economicsl.auctions.singleunit.pricing.MidPointPricingPolicy;

import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.Stream;
import scala.collection.JavaConverters;
import scala.util.Either;
import scala.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ImperativePeriodicDoubleAuction {


    public static void main(String[] args) {

        // define the auction mechanism...
        TestStock googleStock = new TestStock();
        MidPointPricingPolicy<TestStock> midpointPricingPolicy = new MidPointPricingPolicy<>();
        OpenBidSingleUnitAuction<TestStock> doubleAuction = OpenBidSingleUnitAuction.withUniformClearingPolicy(midpointPricingPolicy, googleStock);

        // generate some random order flow...
        int numberOrders = 10000;
        Random prng = new Random(42);
        Stream<Tuple2<UUID, SingleUnitOrder<TestStock>>> orders = OrderGenerator.randomOrders(0.5, numberOrders, googleStock, prng);

        List<Either<Rejected, Accepted>> insertResults = new ArrayList<>();

        for (Tuple2<UUID, SingleUnitOrder<TestStock>> order:JavaConverters.seqAsJavaList(orders)) {
            Tuple2<OpenBidSingleUnitAuction<TestStock>, Either<Rejected, Accepted>> insertResult = doubleAuction.insert(order);
            doubleAuction = insertResult._1();
            insertResults.add(insertResult._2());
        }

        // clear the auction...
        Tuple2<OpenBidSingleUnitAuction<TestStock>, Option<Stream<SpotContract>>> results = doubleAuction.clear();
        List<SpotContract> fills = JavaConverters.seqAsJavaList(results._2().get());

        // print the results to console...
        for (SpotContract fill:fills) {
            System.out.println(fill);
        }

    }

}
