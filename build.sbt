name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.7")

libraryDependencies ++= Seq(
  jdbc,
  filters,
  cache,
  ws,
  guice,
  "com.github.pathikrit" %% "better-files" % "2.17.1"
)
