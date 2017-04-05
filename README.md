[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ae56630986234d08a5944cf802fa04d5)](https://www.codacy.com/app/herculesl/auctions?utm_source=github.com&utm_medium=referral&utm_content=EconomicSL/auctions&utm_campaign=badger)
[![Build Status](https://travis-ci.org/EconomicSL/auctions.svg?branch=develop)](https://travis-ci.org/EconomicSL/auctions)

# auctions
An embedded domain specific language for auction simulation written in Scala and Java 8.

# API Design
Our current API design is motivated by...

* Wurman et al (1998) _Flexible double auctions for electronic commerce: theory and implementation_
* Wellman and Walsh (2001) [_A Parametrization of the Auction Design Space_](https://pdfs.semanticscholar.org/88eb/648f4c74c9e8ee50fd818a266b6f1b3b2ca3.pdf)
* Nisan et al (2007) [_Algorithmic Game Theory_](http://www.cs.cmu.edu/~sandholm/cs15-892F13/algorithmic-game-theory.pdf)

...and our focus at present is on developing a minimal API based around the key ingredients discussed in the Wurman et al (1998) paper. 

## Motivating use cases
Design and simulation of peer-to-peer electricity auctions; market simulation models for "Economy of Things" applications. 
