[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ae56630986234d08a5944cf802fa04d5)](https://www.codacy.com/app/herculesl/auctions?utm_source=github.com&utm_medium=referral&utm_content=EconomicSL/auctions&utm_campaign=badger)
[![Build Status](https://travis-ci.org/EconomicSL/auctions.svg?branch=develop)](https://travis-ci.org/EconomicSL/auctions)

# auctions
An embedded domain specific language for auction simulation written in Scala and Java 8.

# API Design
Our current API design is motivated by...

* Wurman et al (1998) _Flexible double auctions for electronic commerce: theory and implementation_
* Wellman and Walsh (2001) [_A Parametrization of the Auction Design Space_](https://pdfs.semanticscholar.org/88eb/648f4c74c9e8ee50fd818a266b6f1b3b2ca3.pdf)
* Nisan et al (2007) [_Algorithmic Game Theory_](http://www.cs.cmu.edu/~sandholm/cs15-892F13/algorithmic-game-theory.pdf)

...and our focus at present is on developing a minimal API based around the key ingredients discussed in the Wurman et al (1998) paper. Longer term goals include generalizing the auctions EDSL so that auctions are thought of as a special type of social choice mechanism that use "money." 

## Motivating use cases
Design and simulation of electricity auctions (particularly peer-to-peer auctions); market simulation models for "Economy of Things" applications. 

### Design and simulation of electricity auctions
Wholesale electricity auctions are discrete, multi-unit, reverse auctions; electricity auctions differ in the duration for which suppliers' bids are valid; suppliers operate under binding capacity constraints. In order to model wholesale electricity auctions we need an API that supports 

1. Multi-unit auctions
2. Reverse auctions
3. Discrete prices and quantities 
4. Orders that can persist across multiple clearing events

Much ink has been spilt about optimal pricing rules for electricity auctions. In particular, under what circumstances will uniform price auctions outperform (or underperfom!) discrimatory price auctions? In order to address this question the API needs to support both uniform and discriminatory pricing rules.

Currently auctions for electricity are independent of auctions for ancillary energy services, such as storage. In the future, electricity auctions could be combined so that electricity as well as supporting ancillary services can be auctioned together as bundles. This requires that our API be able to handle multi-dimensional or combinatorial auctions.

## "Economy of Things"

TBD
