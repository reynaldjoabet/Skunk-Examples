// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.12"

name := "Skunk-Examples"

version := "1.0"

val skunk = "org.tpolecat" %% "skunk-core" % "0.6.2"

lazy val `scala2-examples` = (project in file("scala2-examples")).settings(
  scalaVersion         := "2.13.12",
  libraryDependencies ++= Seq(skunk)
)

lazy val `scala3-examples` = (project in file("scala3-examples")).settings(
  scalaVersion         := "3.3.1",
  libraryDependencies ++= Seq(skunk),
  scalacOptions ++= Seq(
    "-no-indent"
  )
)
// by default sbt run runs the program in the same JVM as sbt
//in order to run the program in a different JVM, we add the following
//fork in run := true

scalacOptions += "-target:17" // ensures the Scala compiler generates bytecode optimized for the Java 17 virtual machine

//We can also set the soruce and target compatibility for the Java compiler by configuring the JavaOptions in build.sbt

// javaOptions ++= Seq(
//   "-soruce","17","target","17"
// )

ThisBuild / semanticdbEnabled := true

ThisBuild/usePipelining := true
