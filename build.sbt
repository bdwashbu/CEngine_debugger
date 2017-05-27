name := "c_engine_debugger"

lazy val root = (project in file("."))
  .settings(
    name         := "c_engine_debugger",
    organization := "org.c_engine",
    scalaVersion := "2.11.11",
    version      := "0.1.0-SNAPSHOT"
  ).enablePlugins(PlayScala, LauncherJarPlugin)

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "tests"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  jdbc,
  filters,
  cache,
  ws,
  "com.github.pathikrit" %% "better-files" % "2.17.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "com.github.bdwashbu" % "cengine_2.11" % "0.0.4"
)

//testOptions in Test += Tests.Argument("-P")