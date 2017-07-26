package org.economicsl.auctions;


import org.economicsl.auctions.OrderTracker.*;
import org.economicsl.auctions.singleunit.OpenBidAuction;
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
        AuctionProtocol<TestStock> protocol = AuctionProtocol$.MODULE$.apply(googleStock);  // todo create JAuctionProtocol?
        OpenBidAuction<TestStock> doubleAuction = OpenBidAuction.withUniformClearingPolicy(midpointPricingPolicy, protocol);

        // generate some random order flow...
        int numberOrders = 10000;
        Random prng = new Random(42);
        Stream<Tuple2<UUID, SingleUnitOrder<TestStock>>> orders = OrderGenerator.randomSingleUnitOrders(0.5, numberOrders, googleStock, prng);

        List<Either<Rejected, Accepted>> insertResults = new ArrayList<>();

        for (Tuple2<UUID, SingleUnitOrder<TestStock>> order:JavaConverters.seqAsJavaList(orders)) {
            Tuple2<OpenBidAuction<TestStock>, Either<Rejected, Accepted>> insertResult = doubleAuction.insert(order);
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
