// define some common build settings used by core auctions API as well as the various testing configurations
lazy val commonSettings = Seq(
  scalaVersion := "2.12.1" ,
  name := "auctions",
  version := "0.1.0-alpha-SNAPSHOT",
  organization := "org.economicsl",
  organizationName := "EconomicSL",
  organizationHomepage := Some(url("https://economicsl.github.io/")),
  libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % "3.0.1"
  ),
  scalacOptions ++= Seq(
    "-feature",  // tells the compiler to provide information about misused language features
    "-language:implicitConversions"  // eliminates the need to import implicit conversions for each usage
  ),
  compileOrder := CompileOrder.ScalaThenJava
)


// Define additional testing configurations
lazy val Functional = config("functional") extend Test
lazy val Performance = config("performance") extend Test


// finally define the full project build settings
lazy val core = (project in file(".")).
  settings(commonSettings: _*).
  configs(Functional).
  settings(inConfig(Functional)(Defaults.testSettings): _*).
  settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "functional, test"
    ),
    parallelExecution in Functional := false
  ).
  configs(Performance).
  settings(inConfig(Performance)(Defaults.testSettings): _*).
  settings(
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.8.2" % "performance",
    parallelExecution in Performance := false
  )