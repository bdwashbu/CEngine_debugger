name := "c_engine_debugger"

lazy val root = (project in file("."))
  .settings(
    name         := "c_engine_debugger",
    organization := "org.c_engine",
    scalaVersion := "2.11.7",
    version      := "0.1.0-SNAPSHOT"
  ).enablePlugins(PlayScala, LauncherJarPlugin)

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "tests"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  filters,
  cache,
  ws,
  "com.github.pathikrit" %% "better-files" % "2.16.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "org.c_engine" % "c_engine_2.11" % "0.1.0-SNAPSHOT"
)

//testOptions in Test += Tests.Argument("-P")