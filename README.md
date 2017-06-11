[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ae56630986234d08a5944cf802fa04d5)](https://www.codacy.com/app/herculesl/auctions?utm_source=github.com&utm_medium=referral&utm_content=EconomicSL/auctions&utm_campaign=badger)
[![Build Status](https://travis-ci.org/EconomicSL/auctions.svg?branch=develop)](https://travis-ci.org/EconomicSL/auctions)

# auctions
A set of functional APIs for auction simulation written in Scala and Java 8. 

## Installation and usage

### SBT

### Maven
TODO

## API design
Our current API design is based loosely on ideas gleaned from the following.

* Wurman et al (1998) [_Flexible double auctions for electronic commerce: theory and implementation_](http://wewalsh.com/papers/dss98.pdf)
* Wurman et al (2001) [_A Parametrization of the Auction Design Space_](https://pdfs.semanticscholar.org/88eb/648f4c74c9e8ee50fd818a266b6f1b3b2ca3.pdf)
* Nisan et al (2007) [_Algorithmic Game Theory_](http://www.cs.cmu.edu/~sandholm/cs15-892F13/algorithmic-game-theory.pdf)

Primary focus for version 0.1.X was on developing a minimal API based around the key ingredients discussed in the Wurman et al (1998) paper. Rough road map that should take us through the end of 2017:

* Version 0.2.0: will extend the API to allow for multi-unit auctions. Incentive structures in multi-unit auctions often differ in important ways from the incentive structures in single unit auctions. Multi-unit auctions are critical for modeling electricity auctions which are a key use case. 
* Version 0.3.0 will extend the API to incorporate single unit combinatorial auctions. 

In the longer term, we would like to generalize the auctions API so that auctions can be thought of as special types of social choice mechanisms that use "money" (see chapter 10 from Nisan et al (2007) for discussion and details). 

The design and imlementation of our API is heavily influences by functional programming principles and techniques. In particular we strive to...

1. ...minimize (or eliminate!) side effects;
2. ...maintain a thread-safe API in order to maximally exploit concurrency and parallelism where appropriate;
3. ...make illegal program states unrepresentable (i.e., programs that would lead to invalid states should not compile!).

...perhaps more succinctly, we want to push as much of the auction domain modeling into the type system as possible so that we can leverage to compiler to identify modeling errors at compile time (rather than forcing the user to catch modeling errors during/after runtime).

## Motivating use cases
Design and simulation of electricity auctions (particularly peer-to-peer auctions); market simulation models for "Economy of Things" applications. 

### Design and simulation of electricity auctions
Three high-level uses cases: modeling wholesale electricity auctions that are currently being used to price and allocate electricity in many regions and countries around the world; modeling peer-to-peer electricity auctions that are currently being developed to price and allocate (typically renewably generated!) electricity between individual households; combinatoric auctions that could be used to price and allocate bundles of electricity services. 

#### Wholesale electricity markets
Wholesale electricity auctions are discrete, multi-unit, reverse auctions; electricity auctions differ in the duration for which suppliers' bids are valid; suppliers operate under binding capacity constraints. In order to model wholesale electricity auctions we need an API that supports 

1. Multi-unit auctions
2. Reverse auctions
3. Discrete prices and quantities (units matter!)
4. Orders that can persist across multiple clearing events

Much ink has been spilt about optimal pricing rules for wholesale electricity auctions. In particular, under what circumstances will uniform price auctions outperform (or underperfom!) discrimatory price auctions? In order to address this question the API needs to support both uniform and discriminatory pricing rules.

#### Intra-day, real-time electricity markets
Intra-day, real-time electricity auctions are typically operated as continuous double auctions (much like financial markets). Far from obvious that continuous double auctions are optimal auction design for intra-day electricity markets. If real-time auctions should not clear continuously, what is the optimal clearing frequency of intra-day electricity markets?

#### Peer-to-peer electricity auctions
Currently a number of energy sector startups involved in developing technologies that allow individual households to trade electricity with one another using Blockchain-based technologies to process payments. A non-exhaustive list of examples would be...

* [Power Ledger](https://powerledger.io/) Perth, Australia.
* [Grid Singularity](http://gridsingularity.com/), Vienna, Austria.
* [L03 Energy](http://lo3energy.com/), New Yoork, USA.

...how can we optimally design auction mechanisms to facilitate efficient pricing and allocation of electricity in peer-to-peer markets? How relevant is research done at Google, Amazon, et al on design of ecommerce auctions? 

#### Combinatoric electricity auctions
Currently auctions for electricity are independent of auctions for ancillary energy services, such as storage. In the future, electricity auctions could be combined so that electricity as well as supporting ancillary services can be auctioned together as bundles. This requires that our API be able to handle multi-dimensional or combinatorial auctions.

## "Economy of Things"

We have started implementing a [proof-of-concept trading platform](https://github.com/EconomicSL/auctions-remote-example) that would allow Internet-of-Things (IoT) enabled devices to trade with one another using our auction mechanisms.  Motivating use case: explore the impact of auction micro-structure on market outcomes by simulating agents representing "smart houses" trading electricity as well as supporting ancillary services, with one another.
