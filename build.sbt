name := "auctions"

version := "0.1.0-alpha"

scalaVersion := "2.12.1"

// Useful scala compiler options
scalacOptions ++= Seq(
  "-feature",  // tells the compiler to provide information about misused language features
  "-language:implicitConversions"  // eliminates the need to import implicit conversions for each usage
)