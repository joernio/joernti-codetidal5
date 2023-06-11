name := "standalone"
ThisBuild/organization := "org.codeminers"
ThisBuild/scalaVersion := "2.13.8"

// parsed by project/Versions.scala, updated by updateDependencies.sh
val cpgVersion = "1.3.612"
val joernVersion = "1.2.3"
val overflowdbVersion = "1.179"

lazy val schema = Projects.schema
lazy val domainClasses = Projects.domainClasses
lazy val schemaExtender = Projects.schemaExtender

dependsOn(domainClasses)

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.20.0" % Optional,
  "io.joern" %% "x2cpg" % Versions.joern,
  "io.joern" %% "javasrc2cpg" % Versions.joern,
  "io.joern" %% "joern-cli" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern % Test classifier "tests",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)


ThisBuild/Compile/scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
)

enablePlugins(JavaAppPackaging)

ThisBuild/licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

Global/onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/public",
  "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external",
  "Gradle Releases" at "https://repo.gradle.org/gradle/libs-releases/"
)

Compile/doc/sources := Seq.empty
Compile/packageDoc/publishArtifact := false
