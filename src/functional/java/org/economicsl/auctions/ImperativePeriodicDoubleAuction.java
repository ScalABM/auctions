package org.economicsl.auctions;


import org.economicsl.auctions.singleunit.AuctionParticipant.*;
import org.economicsl.auctions.singleunit.OpenBidAuction;
import org.economicsl.auctions.singleunit.OrderGenerator;
import org.economicsl.auctions.singleunit.orders.Order;
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
        MidPointPricingPolicy<GoogleStock> midpointPricingPolicy = new MidPointPricingPolicy<>();
        OpenBidAuction<GoogleStock> doubleAuction = OpenBidAuction.withUniformClearingPolicy(midpointPricingPolicy);

        // generate some random order flow...
        int numberOrders = 10000;
        GoogleStock googleStock = new GoogleStock();
        Random prng = new Random(42);
        Stream<Tuple2<UUID, Order<GoogleStock>>> orders = OrderGenerator.randomOrders(0.5, numberOrders, googleStock, prng);

        List<Either<Rejected, Accepted>> insertResults = new ArrayList<>();

        for (Tuple2<UUID, Order<GoogleStock>> order:JavaConverters.seqAsJavaList(orders)) {
            Tuple2<OpenBidAuction<GoogleStock>, Either<Rejected, Accepted>> insertResult = doubleAuction.insert(order);
            doubleAuction = insertResult._1();
            insertResults.add(insertResult._2());
        }

        // clear the auction...
        Tuple2<OpenBidAuction<GoogleStock>, Option<Stream<Fill>>> results = doubleAuction.clear();
        List<Fill> fills = JavaConverters.seqAsJavaList(results._2().get());

        // print the results to console...
        for (Fill fill:fills) {
            System.out.println(fill);
        }

    }

}
