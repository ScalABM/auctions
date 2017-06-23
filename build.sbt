// define some common build settings used by core auctions API as well as the various testing configurations
lazy val commonSettings = Seq(
  scalaVersion := "2.12.2" ,
  name := "esl-auctions",
  version := "0.2.0-SNAPSHOT",
  organization := "org.economicsl",
  organizationName := "EconomicSL",
  organizationHomepage := Some(url("https://economicsl.github.io/")),
  libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % "3.0.1",
    "com.typesafe.akka" %% "akka-actor" % "2.5.2",
    "com.typesafe.play" %% "play-json" % "2.6.0-RC2",
    "org.economicsl" %% "esl-core" % "0.1.0-SNAPSHOT"
  ),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
  ),
  javacOptions ++= Seq(
    "-Xlint:unchecked"
  ),
  scalacOptions ++= Seq(
    "-deprecation",  // issue warning if we use any deprecated API features
    "-feature",  // tells the compiler to provide information about misused language features
    "-language:implicitConversions",  // eliminates the need to import implicit conversions for each usage
    "-language:reflectiveCalls",  // needed in order to enable structural (or duck) typing
    "-Xlint",
    "-Ywarn-unused-import",
    "-Ywarn-dead-code"
  ),
  compileOrder := CompileOrder.ScalaThenJava
)


// Define additional testing configurations
lazy val Functional = config("functional") extend Test


// finally define the full project build settings
lazy val core = (project in file(".")).
  settings(commonSettings: _*).
  configs(Functional).
  settings(inConfig(Functional)(Defaults.testSettings): _*).
  settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "functional, test",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.2" % "functional, test"
    ),
    parallelExecution in Functional := true
  )
