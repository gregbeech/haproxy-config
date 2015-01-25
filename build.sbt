lazy val root = (project in file(".")).
  settings(
    name := "haproxy-config",
    organization := "com.gregbeech",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.5",
    crossScalaVersions := Seq("2.11.5"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= Seq(
      "org.parboiled" %% "parboiled" % "2.0.1",
      "org.scalatest" %% "scalatest" % "2.2.1" % Test
    )
  )