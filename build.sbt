name := "azure-sdk-for-scala"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.microsoft.azure" % "adal4j" % "1.1.2",
  "com.microsoft.azure" % "azure-client-runtime" % "1.0.0-beta2",
  "com.typesafe" % "config" % "1.3.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.2",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
