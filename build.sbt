name := "auctions"

version := "0.1.0-alpha"

scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-Yno-predef"  // no automatic import of the Predef object (which removes irritating implicits)
)