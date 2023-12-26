
// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"


name := "Skunk-Examples"

version := "1.0"

libraryDependencies += "org.tpolecat" %% "skunk-core" % "0.6.2"

// by default sbt run runs the program in the same JVM as sbt
//in order to run the program in a different JVM, we add the following
fork in run := true