package org.economicsl.auctions


sealed trait Security extends Tradable

class GoogleStock extends Security