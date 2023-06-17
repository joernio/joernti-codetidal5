name := "joernti-codetidal5"
ThisBuild / organization := "io.joern"
ThisBuild / scalaVersion := "2.13.8"

// parsed by project/Versions.scala, updated by updateDependencies.sh
val cpgVersion = "1.3.612"
val joernVersion = "1.2.12"
val overflowdbVersion = "1.179"

val astGenVersion = "3.1.0"

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.20.0" % Optional,
  "io.joern" %% "x2cpg" % Versions.joern,
  "io.joern" %% "jssrc2cpg" % Versions.joern,
  "io.joern" %% "joern-cli" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern % Test classifier "tests",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)


ThisBuild / Compile / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
)

enablePlugins(JavaAppPackaging)

ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/public",
  "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external",
  "Gradle Releases" at "https://repo.gradle.org/gradle/libs-releases/"
)

Compile / doc / sources := Seq.empty
Compile / packageDoc / publishArtifact := false

lazy val AstgenWin = "astgen-win.exe"
lazy val AstgenLinux = "astgen-linux"
lazy val AstgenLinuxArm = "astgen-linux-arm"
lazy val AstgenMac = "astgen-macos"
lazy val AstgenMacArm = "astgen-macos-arm"

lazy val astGenDlUrl = settingKey[String]("astgen download url")
astGenDlUrl := s"https://github.com/joernio/astgen/releases/download/v${astGenVersion}/"

lazy val astGenBinaryNames = taskKey[Seq[String]]("astgen binary names")
astGenBinaryNames := {
  if (sys.props.get("ALL_PLATFORMS").contains("TRUE")) {
    Seq(AstgenWin, AstgenLinux, AstgenMac, AstgenMacArm)
  } else {
    Environment.operatingSystem match {
      case Environment.OperatingSystemType.Windows =>
        Seq(AstgenWin)
      case Environment.OperatingSystemType.Linux =>
        Environment.architecture match {
          case Environment.ArchitectureType.X86 => Seq(AstgenLinux)
          case Environment.ArchitectureType.ARM => Seq(AstgenLinuxArm)
        }
      case Environment.OperatingSystemType.Mac =>
        Environment.architecture match {
          case Environment.ArchitectureType.X86 => Seq(AstgenMac)
          case Environment.ArchitectureType.ARM => Seq(AstgenMacArm)
        }
      case Environment.OperatingSystemType.Unknown =>
        Seq(AstgenWin, AstgenLinux, AstgenMac, AstgenMacArm)
    }
  }
}

lazy val astGenDlTask = taskKey[Unit](s"Download astgen binaries")
astGenDlTask := {
  val astGenDir = baseDirectory.value / "bin" / "astgen"
  astGenDir.mkdirs()

  astGenBinaryNames.value.foreach { fileName =>
    val dest = astGenDir / fileName
    if (!dest.exists) {
      val url = s"${astGenDlUrl.value}$fileName"
      val downloadedFile = SimpleCache.downloadMaybe(url)
      IO.copyFile(downloadedFile, dest)
    }
  }

  val distDir = (Universal / stagingDirectory).value / "bin" / "astgen"
  distDir.mkdirs()
  IO.copyDirectory(astGenDir, distDir)

  // permissions are lost during the download; need to set them manually
  astGenDir.listFiles().foreach(_.setExecutable(true, false))
  distDir.listFiles().foreach(_.setExecutable(true, false))
}

lazy val astGenSetAllPlatforms = taskKey[Unit](s"Set ALL_PLATFORMS")
astGenSetAllPlatforms := {
  System.setProperty("ALL_PLATFORMS", "TRUE")
}

stage := Def
  .sequential(astGenSetAllPlatforms, Universal / stage)
  .andFinally(System.setProperty("ALL_PLATFORMS", "FALSE"))
  .value

// Also remove astgen binaries with clean, e.g., to allow for updating them.
// Sadly, we can't define the bin/ folders globally,
// as .value can only be used within a task or setting macro
cleanFiles ++= Seq(
  baseDirectory.value / "bin" / "astgen",
  (Universal / stagingDirectory).value / "bin" / "astgen"
) ++ astGenBinaryNames.value.map(fileName => SimpleCache.encodeFile(s"${astGenDlUrl.value}$fileName"))

